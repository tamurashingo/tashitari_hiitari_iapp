import com.nttdocomo.ui.*;
import java.io.*;
import javax.microedition.io.*;
import java.util.Random;

public class pC extends Canvas implements Runnable {
	
    ksan k;
    /**
     * ステータス。
     * 0: 初期設定
     * 1: タイトル画面
     * 2: プレイ中
     * 3: ゲームオーバー
     * 4: 終了処理
     */
    int stat;
    /** 落ちてくる数字のY座標 */
    int sy[] = new int[9];
    /** 何ループで落ちてくるかのリミット */
    int sl[] = new int[9];
    /** 落ちてくる数字が何か */
    int st[] = new int[9];
    /** ksanのX座標 */
    int x;

    /** 次に揃える数(計算用) */
    int next;
    /** 次に揃える数(表示用) */
    int ne[] = new int[2];
    /** 右側に積む数 */
    int stack[] = new int[9];
    /** スタックの位置 */
    int stu;
    /** スコア */
    int score;
    /** ハイスコア */
    int hi;
    int lv;

    Image n[] = new Image[23];
    Graphics g;
    Random r = new Random();


    public pC() {}

    public void init(ksan ks) {
        int i;
        MediaImage mi;
        setSoftLabel(Frame.SOFT_KEY_1, "EXIT");
        setBackground(Graphics.getColorOfName(Graphics.BLACK));
        g = this.getGraphics();
        this.k = ks;
        try {
            for( i=0; i<23; i++ ) {
                mi = MediaManager.getImage("resource:///"+i+".gif");
                mi.use();
                n[i] = mi.getImage();
            }
        } catch (Exception e) {}
        for( i=0; i<9; i++ ) {
            sy[i] = -Math.abs(r.nextInt()) % 110 - 16;
            st[i] = Math.abs(r.nextInt()) % 20;
            sl[i] = 0;
            stack[i] = 99;
        }
        stat = 0;
        x = 51;
        ne[0] = Math.abs(r.nextInt()) % 2;
        ne[1] = Math.abs(r.nextInt()) % 10;
        next = ne[0]*10+ne[1];
        stu = 0;
        score = 0;
        lv = 30;
        read();
    }

    public void go() {
        Thread th = new Thread(this);
        th.start();
    }

    public void read() {
        try {
            InputStream in = Connector.openInputStream("scratchpad:///0");
            hi = in.read() << 24 | in.read() << 16 | in.read() << 8 | in.read();
            in.close();
        } catch (IOException e) {}
    }

    public void save() {
        if (score > hi) {
            try {
                OutputStream out = Connector.openOutputStream("scratchpad:///0");
                out.write((score >> 24) & 0xff);
                out.write((score >> 16) & 0xff);
                out.write((score >> 8) & 0xff);
                out.write(score & 0xff);
                out.close();
            } catch (IOException e) {}
            hi = score;
        }
    }

    public void run() {
        int i, j, m, num;
        long a, b;
        for(;;){
            a = System.currentTimeMillis() + 50;
            handleKeyState();

            switch (stat) {
            case 0:
                setSoftLabel(Frame.SOFT_KEY_2, "PLAY");
                stat = 1;
            case 1:
                break;
            case 2:
                for( i=0; i<9; i++ ) {
                    sl[i]--;
                    if (sl[i] < 0) {
                        sy[i] += 4;
                        if (sy[i] > 100) {
                            sy[i] = -Math.abs(r.nextInt()) % 110 - 16;
                            st[i] = Math.abs(r.nextInt()) % 20;
                        }
                        sl[i] = lv / 3;
                    }
                    
                    if (sy[i]>=76 && sy[i]<=96 && (x+4>=i*10 && x-12<=i*10)) {
                        stack[stu] = st[i];
                        sy[i] = -Math.abs(r.nextInt()) % 110 - 16;
                        st[i] = Math.abs(r.nextInt()) % 20;
                        for( j=0, m=0, num=0; j<9; j++ ) {
                            if (stack[j] != 99) {
                                if (stack[j] > 9) m = m - stack[j] + 10;
                                else m = m + stack[j];
                                num += j*2 + 1;
                            }
                        }
                        if (m == next) {
                            ne[0] = Math.abs(r.nextInt()) % 2;
                            ne[1] = Math.abs(r.nextInt()) % 10;
                            next = ne[0]*10+ne[1];
                            stu = 0;
                            score += num;
                            lv--;
                            if (lv < 1) lv = 1;
                            for( j=0; j<9; j++ ) stack[j] = 99;
                        } else {
                            stu++;
                            if (stu==9) stat = 3;
                        }
                    }
                }
                break;
            case 3:
                break;
            case 4:
                save();
                stat = 0;
                x = 51;
                ne[0] = Math.abs(r.nextInt()) % 2;
                ne[1] = Math.abs(r.nextInt()) % 10;
                next = ne[0]*10+ne[1];
                stu = 0;
                score = 0;
                lv = 30;
                for( i=0; i<9; i++ ) {
                    sy[i] = -Math.abs(r.nextInt()) % 110 - 16;
                    st[i] = Math.abs(r.nextInt()) % 20;
                    sl[i] = lv;
                    stack[i] = 99;
                }
            }
            repaint();
            System.gc();
            b = System.currentTimeMillis();
            if (b < a) {
                try { Thread.sleep(a-b); } catch (Exception e) {}
            }
        }
    }

    public void paint(Graphics g) {
        g.lock();
        int i, j;
        switch (stat) {
        case 0:
        case 1:
            i = hi;
            j = (getWidth()-80)/2;
            g.setColor(g.getColorOfName(Graphics.BLACK));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.drawImage(n[22], (getWidth()-56)/2, 54);
            g.setColor(g.getColorOfName(Graphics.WHITE));
            g.drawString("HI SCORE", j, 85);
            g.drawImage(n[i/100000000], j    , 91); i %= 100000000;
            g.drawImage(n[i/ 10000000], j + 8, 91); i %= 10000000;
            g.drawImage(n[i/  1000000], j +16, 91); i %= 1000000;
            g.drawImage(n[i/   100000], j +24, 91); i %= 100000;
            g.drawImage(n[i/    10000], j +32, 91); i %= 10000;
            g.drawImage(n[i/     1000], j +40, 91); i %= 1000;
            g.drawImage(n[i/      100], j +48, 91); i %= 100;
            g.drawImage(n[i/       10], j +56, 91); i %= 10;
            g.drawImage(n[i          ], j +64, 91);
            g.drawImage(n[21], j+72, 91);
            break;
        case 2:
            g.setColor(g.getColorOfName(Graphics.BLACK));
            g.fillRect(0, 0, 100, 100);
            g.setColor(g.getColorOfName(Graphics.GRAY));
            g.fillRect(100, 0, getWidth(), getHeight());
            g.setColor(g.getColorOfName(Graphics.BLACK));

            for( i=0; i<9; i++ ) {
                if (sy[i] > 0) {
                    g.drawImage(n[st[i]], i*11+2, sy[i]);
                }
		
                if (stack[8-i] != 99) {
                    g.drawImage(n[stack[8-i]], 106, i*10+12);
                } else {
                    g.fillRect(106, i*10+12, 8, 8);
                }
            }
            g.drawImage(n[20], x, 85);
            g.setColor(g.getColorOfName(Graphics.GRAY));
            g.fillRect(0, 100, getWidth(), getHeight());
            g.setColor(g.getColorOfName(Graphics.BLACK));
            g.fillRect(5, 104, 90, 12);

            i = score;
            g.drawImage(n[i/100000000],  7, 106); i %= 100000000;
            g.drawImage(n[i/ 10000000], 15, 106); i %= 10000000;
            g.drawImage(n[i/  1000000], 23, 106); i %= 1000000;
            g.drawImage(n[i/   100000], 31, 106); i %= 100000;
            g.drawImage(n[i/    10000], 39, 106); i %= 10000;
            g.drawImage(n[i/     1000], 47, 106); i %= 1000;
            g.drawImage(n[i/      100], 55, 106); i %= 100;
            g.drawImage(n[i/       10], 63, 106); i %= 10;
            g.drawImage(n[i          ], 71, 106);


            g.drawImage(n[21], 80, 106);
            g.drawImage(n[ne[0]], 102, 2);
            g.drawImage(n[ne[1]], 110, 2);

            break;
        case 3:
            g.drawImage(n[stack[8]], 106, 12);
            g.setColor(g.getColorOfName(Graphics.WHITE));
            g.drawString("GAME OVER", 13, 70);
            if (hi <= score && score != 0)
                g.drawString("HI SCORE!", 13, 82);
            g.setColor(g.getColorOfName(Graphics.LIME));
            g.drawString("GAME OVER", 12, 69);
            break;
        case 4:
            break;
        }
        g.unlock(true);
    }

    public void handleKeyState() {
        int key = this.getKeypadState();
        if ((key&(1<<Display.KEY_SOFT1)) != 0) {
            k.terminate();
        }

        switch (stat) {
        case 0:
        case 1:
        case 4:
            if ((key&(1<<Display.KEY_SOFT2)) != 0) {
                while((key&(1<<Display.KEY_SOFT2)) != 0) key = this.getKeypadState();
                stat = 2;
            }
            break;
        case 2:
            if ((key&(1<<Display.KEY_RIGHT)) != 0) {
                x += 4;
                if (x>84) {
                    x = 84;
                }
            }

            if ((key&(1<<Display.KEY_LEFT)) != 0) {
                x -= 4;
                if (x<0) {
                    x = 0;
                }
            }
            break;
        case 3:
            if ((key&(1<<Display.KEY_SOFT2)) != 0) {
                while((key&(1<<Display.KEY_SOFT2)) != 0) key = this.getKeypadState();
                stat = 4;
            }
            break;
        }
    }
}
