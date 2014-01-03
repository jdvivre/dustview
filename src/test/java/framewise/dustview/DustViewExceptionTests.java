package framewise.dustview;

import org.junit.Test;
import static org.junit.Assert.*;
import static  org.hamcrest.CoreMatchers.*;

/**
 * @author chanwook
 */
public class DustViewExceptionTests {

    @Test
    public void exceptionalInfo(){
        String msg = "msg";
        IllegalArgumentException caused = new IllegalArgumentException();

        DustViewException ex = new DustViewException(msg);
        assertThat(ex.getMessage(), is(msg));
        assertThat(ex.getCause(), is(nullValue()));

        ex = new DustViewException(caused);
        System.out.print(ex.getMessage());
        assertThat(ex.getMessage(), is(caused.getClass().getName()));
        assertThat(ex.getCause() instanceof IllegalArgumentException, is(true));

        ex = new DustViewException(msg, caused);
        assertThat(ex.getMessage(), is(msg));
        assertThat(ex.getCause() instanceof IllegalArgumentException, is(true));

    }
}
