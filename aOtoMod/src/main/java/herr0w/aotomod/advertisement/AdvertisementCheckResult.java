package herr0w.aotomod.advertisement;

public record AdvertisementCheckResult(boolean detected, String type, String detectedValue) {
    public static AdvertisementCheckResult clean() {
        return new AdvertisementCheckResult(false, "", "");
    }
}
