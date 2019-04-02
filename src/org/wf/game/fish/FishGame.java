package org.wf.game.fish;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class FishGame {
    /**
     * @param args
     * @author lonely wolf
     * @version 捕鱼1.0
     * @time 2015.06.19
     */
    public static void main(String[] args) {
        //游戏画框
        JFrame jf = new JFrame("疯狂捕鱼");
        jf.setSize(800, 480);
        jf.setLocationRelativeTo(null);
        jf.setResizable(false);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setAlwaysOnTop(true);
        //加载鱼池
        Pool pool = new Pool();
        jf.add(pool);
        //显示框架
        jf.setVisible(true);
        pool.action();
    }
}

//鱼池类
class Pool extends JPanel {
    private static final long serialVersionUID = 1L;
    BufferedImage bgImage;    //背景图片
    Fish[] fishs = new Fish[20];    //所有的鱼
    Net net = new Net();    //渔网
    boolean isExit;    //鼠标是否在游戏界面
    int score, bullet = 50;    //游戏得分,子弹数

    public Pool() {
        super();
        File bg = new File("images/bg.jpg");
        try {
            bgImage = ImageIO.read(bg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //0-8,9-17对应1-9号鱼
        for (int i = 0; i < fishs.length / 2 - 1; i++) {
            fishs[i] = new Fish(i + 1);
            fishs[i + 9] = new Fish(i + 1);
        }
        fishs[18] = new Fish(10);
        fishs[19] = new Fish(11);
    }

    //画游戏元素
    @Override
    public void paint(Graphics g) {
        g.drawImage(bgImage, 0, 0, null);    //画背景
        for (Fish fish : fishs) {
            g.drawImage(fish.fishImage, fish.fish_x, fish.fish_y, null);    //画鱼
        }
        if (!isExit) {
            g.drawImage(net.netImage, net.netX, net.netY, null);    //画网
        }

        //画游戏说明文字
        g.setColor(Color.GREEN);
        g.setFont(new Font("楷体", Font.ITALIC, 20));
        g.drawString("疯狂捕鱼V1.0 By~Synchronized", 10, 25);
        g.drawString("子弹数:" + bullet + "   得分:" + score, 350, 25);
        g.drawString("右键切换渔网  VIP:" + (net.power % 7 + 1), 590, 25);
        if (bullet <= 0) {
            g.setColor(Color.RED);
            g.setFont(new Font("楷体", Font.BOLD, 100));
            g.drawString("Game Over", 150, 250);
            bullet = 0;
            isExit = true;
            net.power = -1;
        }
    }

    //游戏启动方法
    public void action() {
        for (Fish fish : fishs) {
            fish.start();
        }
        //鼠标监听器
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int event = e.getModifiers();
                if (event == 4) {
                    net.change();    //切换网大小
                    super.mousePressed(e);
                } else if (event == 16) {
                    //减子弹
                    if (bullet - (net.power % 7 + 1) <= 0) {
                        bullet = 0;
                    } else {
                        bullet -= (net.power % 7 + 1);
                    }
                    //捕鱼
                    for (Fish fish : fishs) {
                        if (!fish.catched) {
                            catchFish(fish);
                        }
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                net.moveTo(e.getX(), e.getY());
                super.mouseMoved(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                isExit = false;
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isExit = true;
                super.mouseExited(e);
            }
        };
        this.addMouseListener(adapter);
        this.addMouseMotionListener(adapter);
        while (true) {
            repaint();
        }
    }

    //封装捕鱼的方法
    public void catchFish(Fish fish) {
        fish.catched = net.catchFish(fish);
        if (fish.catched) {
            score += 2 * fish.k;
            bullet += 2 * fish.k;
        }
    }
}

//鱼类
class Fish extends Thread {
    int fish_x, fish_y;    //鱼的坐标
    BufferedImage fishImage;    //鱼的图片
    BufferedImage[] fishImages = new BufferedImage[10];    //鱼动作的图片
    BufferedImage[] catchImages;    //鱼的被捕的图片
    int fish_width, fish_height;    //鱼的宽高
    Random r = new Random();    //鱼y坐标的随机数
    int blood;    //鱼的血量值
    boolean catched;    //鱼是否被捕
    int k, step_size;    //鱼的血量等级,移动速度

    public Fish(int m) {
        super();
        String preName = m > 9 ? m + "" : "0" + m;
        //通过for循环读取鱼动作图片数组
        for (int i = 0; i < fishImages.length; i++) {
            int j = i + 1;
            String lastName = j > 9 ? "10" : "0" + j;
            File file = new File("images/fish" + preName + "_" + lastName + ".png");
            try {
                fishImages[i] = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fishImage = fishImages[0];
        fish_width = this.fishImage.getWidth();
        fish_height = this.fishImage.getHeight();
        fish_x = 800;
        fish_y = r.nextInt(450 - fish_height);
        blood = m * 3;
        this.k = m;
        step_size = r.nextInt(5) + 1;
        //初始化catchImages
        if (m > 7) {
            catchImages = new BufferedImage[4];
        } else if (m <= 7) {
            catchImages = new BufferedImage[2];
        }
        //通过for循环读取鱼被捕图片数组
        for (int i = 1; i <= catchImages.length; i++) {
            File file = new File("images/fish" + preName + "_catch_0" + i + ".png");
            try {
                catchImages[i - 1] = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //鱼移动的方法
    public void move() {
        fish_x -= step_size;
    }

    @Override
    public void run() {
        super.run();
        while (true) {
            move();    //调用鱼移动的方法
            //如果鱼出界,重新生成
            if (fish_x < -fish_width || catched) {
                turnOut();    //鱼被捕,颤动
                newFish();
            }
            change();    //调用鱼摇摆游动的方法
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //重新生成一条鱼
    public void newFish() {
        fish_x = 800;
        fish_y = r.nextInt(450 - fish_height);
        if (fish_y >= 450) {
            // System.out.println(fish_height + "  " + fish_y);
        }
        catched = false;
        blood = k * 3;
        step_size = r.nextInt(5) + 1;
    }

    // 鱼摇摆游动的方法
    int index = 0;

    public void change() {
        index++;
        fishImage = fishImages[index / 3 % 10];
    }

    //鱼被捕动画的方法
    public void turnOut() {
        for (int i = 0; i < catchImages.length; i++) {
            fishImage = catchImages[i];
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

//渔网类
class Net {
    int netX, netY;//网的坐标
    int netWidth, netHeight;//网的宽高
    BufferedImage netImage;//网的图片

    public Net() {
        super();
        File file = new File("images/net_" + 1 + ".png");
        try {
            netImage = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        netX = 300;
        netY = 300;
        netWidth = netImage.getWidth();
        netHeight = netImage.getHeight();
    }

    // 鱼网切换的方法
    int power = 0;

    public void change() {
        power++;
        int x = this.netX + this.netWidth / 2;
        int y = this.netY + this.netHeight / 2;
        File file = new File("images/net_" + (power % 7 + 1) + ".png");
        try {
            netImage = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.netWidth = netImage.getWidth();
        this.netHeight = netImage.getHeight();
        moveTo(x, y);
    }

    //渔网移动的方法
    public void moveTo(int x, int y) {
        this.netX = x - this.netWidth / 2;
        this.netY = y - this.netHeight / 2;
    }

    //捕鱼
    public boolean catchFish(Fish fish) {
        //网的中心坐标
        int zX = netX + netWidth / 2;
        int zY = netY + netHeight / 2;
        //鱼的身体部分中心坐标
        int fX = fish.fish_x + fish.fish_width * 2 / 3;
        int fY = fish.fish_y + fish.fish_height / 2;
        //如果网的中心坐标在鱼的身体部分
        if (zX > fish.fish_x && zX < fish.fish_x + fish.fish_width * 2 / 3
                && zY > fish.fish_y && zY < fish.fish_y + fish.fish_height) {
            fish.blood -= ((power % 7 + 1) * 2);
            // System.out.println(fish.blood);
        } else if (fX > netX && fX < netX + netWidth
                && fY > netY && fY < netY + netHeight) {
            //如果鱼的身体部分中心坐标在网里
            fish.blood -= ((power % 7 + 1) * 2);
            // System.out.println(fish.blood);
        }
        return fish.blood <= 0;
    }
}