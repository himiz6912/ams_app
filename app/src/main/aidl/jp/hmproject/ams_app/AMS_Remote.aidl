// AMS_Remote.aidl
package jp.hmproject.ams_app;

// Declare any non-default types here with import statements
import jp.hmproject.ams_app.AMS_Callback;

interface AMS_Remote {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void registerCallback(AMS_Callback ac);
    void unregisterCallback(AMS_Callback ac);
    void command(String msg);
}
