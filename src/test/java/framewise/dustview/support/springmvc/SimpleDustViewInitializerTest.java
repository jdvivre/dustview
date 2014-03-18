package framewise.dustview.support.springmvc;

import framewise.dustview.support.DustViewConstants;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Created by chanwook on 2014. 2. 18..
 */
public class SimpleDustViewInitializerTest {

    @Test
    public void changeCompiled() {
        SimpleDustViewInitializer i = new SimpleDustViewInitializer();
        SimpleDustTemplateView v = new SimpleDustTemplateView();

        // check init value
        assertTrue(v.isCompiled());

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put(DustViewConstants.DUST_COMPILED, false);

        i.initializeViewProperty(map, v);

        assert(v.isCompiled());
    }
}
