package lm.pkp.com.landmap.custom;

import lm.pkp.com.landmap.position.PositionElement;

/**
 * Created by USER on 10/17/2017.
 */
public interface LocationPositionReceiver {

    void receivedLocationPostion(PositionElement pe);

    void locationFixTimedOut();

    void providerDisabled();
}
