package io.github.ndellagrotte.doabarrelroll;

/**
 * Build-time constants. The {@code @{...}} markers are replaced by Token Envoy during compilation,
 * see tokens.properties and build.gradle.
 */
public final class Reference {

    private Reference() {}

    public static final String MOD_ID = "@{MOD_ID}";
    public static final String MOD_NAME = "@{MOD_NAME}";
    public static final String VERSION = "@{MOD_VERSION}";

}
