import java.io.Closeable;

/**
 * �ر���Դ
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
