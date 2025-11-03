package net.pixeldreamstudios.mswcompat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.pixeldreamstudios.mswcompat.config.MSWCompatConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MSWCompat implements ModInitializer {
	public static final String MOD_ID = "msw-compat";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		MSWCompatConfig.load(FabricLoader.getInstance().getConfigDir());
		LOGGER.info("MSW-Compat initialized!");
	}
}