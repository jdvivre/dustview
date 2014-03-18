package framewise.dustview.support;

import framewise.dustview.support.springmvc.SimpleDustTemplateView;

import java.util.Map;

/**
 * @author chanwook
 */
public interface DustViewInitializer {

    void initializeViewProperty(Map<String, Object> attributesMap, SimpleDustTemplateView simpleDustTemplateView);

}
