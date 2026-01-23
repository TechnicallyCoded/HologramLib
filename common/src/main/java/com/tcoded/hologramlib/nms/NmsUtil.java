package com.tcoded.hologramlib.nms;

import com.tcoded.hologramlib.HologramLib;
import com.tcoded.hologramlib.manager.HologramManager;

import java.lang.reflect.Constructor;
import java.util.Map;

public class NmsUtil {

    private static final Map<String, String> versionMap = Map.of();

    public static <InternalIdType> HologramManager<InternalIdType> findManagerClass(HologramLib<InternalIdType> lib, String mcVersion) {
        String mappedVersion = versionMap.getOrDefault(mcVersion, mcVersion);
        String parsedVersion = mappedVersion.replace('.', '_');

        try {
            String rawClassName = HologramLib.class.getPackageName() + ".nms.v{VERSION}.NmsHologramManager";
            String versionedClassName = rawClassName.replace("{VERSION}", parsedVersion);

            Class<?> clazz = Class.forName(versionedClassName);
            Constructor<?> constructor = clazz.getConstructor(HologramLib.class);

            // noinspection unchecked
            return (HologramManager<InternalIdType>) constructor.newInstance(lib);
        } catch (Exception e) {
            // noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return null;
    }

}
