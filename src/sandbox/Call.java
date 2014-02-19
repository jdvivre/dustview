import framewise.dustview.core.DustTemplateEngineTest;
import org.junit.Test;

/**
 * Created by chanwook on 2014. 1. 15..
 */
public class Call {
    @Test
    public  void call() throws Exception {
        DustTemplateEngineTest t = new DustTemplateEngineTest();

        for(int i=0; i < 10000000000L; i++) {
            t.compile2load2renderinDustJsSource();
            System.out.print("end");
        }
    }
}
