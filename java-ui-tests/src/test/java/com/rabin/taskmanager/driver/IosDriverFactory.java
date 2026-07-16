package com.rabin.taskmanager.driver;

import com.rabin.taskmanager.config.TestConfig;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

import java.net.MalformedURLException;

public final class IosDriverFactory {
    private final TestConfig config;

    public IosDriverFactory(TestConfig config) {
        this.config = config;
    }

    public IOSDriver create() throws MalformedURLException {
        XCUITestOptions options = new XCUITestOptions()
                .setDeviceName(config.deviceName())
                .setAutomationName("XCUITest")
                .setBundleId(config.bundleId())
                .setNoReset(config.noReset());

        if (!config.platformVersion().isBlank()) {
            options.setPlatformVersion(config.platformVersion());
        }

        if (!config.udid().isBlank()) {
            options.setUdid(config.udid());
        }

        setOptionalCapability(options, "appium:derivedDataPath", "ios.derived.data.path");
        setOptionalCapability(options, "appium:showXcodeLog", "ios.show.xcode.log");
        setOptionalCapability(options, "appium:wdaStartupRetries", "ios.wda.startup.retries");
        setOptionalCapability(options, "appium:wdaStartupRetryInterval", "ios.wda.startup.retry.interval");
        setOptionalCapability(options, "appium:xcodeOrgId", "ios.xcode.org.id");
        setOptionalCapability(options, "appium:xcodeSigningId", "ios.xcode.signing.id");
        setOptionalCapability(options, "appium:xcodeConfigFile", "ios.xcode.config.file");
        setOptionalCapability(options, "appium:updatedWDABundleId", "ios.updated.wda.bundle.id");
        setOptionalCapability(options, "appium:allowProvisioningDeviceRegistration", "ios.allow.provisioning.device.registration");
        setOptionalCapability(options, "appium:wdaLaunchTimeout", "ios.wda.launch.timeout");
        setOptionalCapability(options, "appium:wdaConnectionTimeout", "ios.wda.connection.timeout");

        return new IOSDriver(config.appiumServer(), options);
    }

    private void setOptionalCapability(XCUITestOptions options, String capabilityName, String propertyName) {
        String value = config.property(propertyName, "");
        if (!value.isBlank()) {
            options.setCapability(capabilityName, value);
        }
    }
}
