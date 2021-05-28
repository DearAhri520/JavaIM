import java.io.Closeable;

/**
 * ¹Ø±Õ×ÊÔ´
 * @author DearAhri520
 */
class Close {
     static void close(Closeable... targets) {
        for(Closeable target:targets) {
            try {
                if(target!=null) {
                    target.close();
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
