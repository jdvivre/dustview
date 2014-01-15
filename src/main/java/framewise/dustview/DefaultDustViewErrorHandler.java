package framewise.dustview;

/**
 * @author chanwook
 */
public class DefaultDustViewErrorHandler implements DustViewErrorHandler {
    @Override
    public void checkError(String templateKey, String renderedView) {
        //handling error
        if (renderedView.startsWith("Error: Template Not Found:")) {
            throw new DustViewException("Throwed exception when redering to resource(templateKey: " + templateKey + ")\n caused: " + renderedView);
        }
    }
}
