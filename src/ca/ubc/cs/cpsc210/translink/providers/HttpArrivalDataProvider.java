package ca.ubc.cs.cpsc210.translink.providers;

import ca.ubc.cs.cpsc210.translink.auth.TranslinkToken;
import ca.ubc.cs.cpsc210.translink.model.Stop;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Wrapper for Translink Arrival Data Provider
 */
public class HttpArrivalDataProvider extends AbstractHttpDataProvider {
    private Stop stop;

    public HttpArrivalDataProvider(Stop stop) {
        super();
        this.stop = stop;
    }

    @Override
    /**
     * Produces URL used to query Translink web service for expected arrivals at
     * the stop specified in call to constructor.
     *
     * @returns URL to query Translink web service for arrival data
     */
    protected URL getUrl() throws MalformedURLException { //task 8
        URL result = null;
        if (stop != null) {
            Number stopNo = stop.getNumber();
            String stopNoStr = stopNo.toString();
            String myApi = TranslinkToken.TRANSLINK_API_KEY;
            String url = "http://api.translink.ca/rttiapi/v1/stops/" + stopNo + "/estimates?apikey=" + myApi;
            URL url1 = new URL(url);
            result = url1;
        }
        return result;
    }


    @Override
    public byte[] dataSourceToBytes() throws IOException {
        return new byte[0];
    }
}
