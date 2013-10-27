import com.nttdocomo.ui.*;

public class ksan extends IApplication {
    public void start() {
        pC p = new pC();
        p.init(this);
        Display.setCurrent(p);
        p.go();
    }
}
