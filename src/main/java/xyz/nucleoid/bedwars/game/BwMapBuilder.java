package xyz.nucleoid.bedwars.game;

import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import xyz.nucleoid.bedwars.BedWars;
import xyz.nucleoid.bedwars.game.config.BwConfig;
import xyz.nucleoid.bedwars.game.generator.BwSkyMapBuilder;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateMetadata;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.api.game.world.generator.TemplateChunkGenerator;

import java.io.IOException;

public final class BwMapBuilder {
    private final BwConfig config;

    public BwMapBuilder(BwConfig config) {
        this.config = config;
    }

    public BwMap create(MinecraftServer server) {
        return this.config.map().map(
                skyConfig -> new BwSkyMapBuilder(this.config, skyConfig).build(server),
                path -> {
                    MapTemplate template;
                    try {
                        template = MapTemplateSerializer.loadFromResource(server, path);
                    } catch (IOException e) {
                        template = MapTemplate.createEmpty();
                        BedWars.LOGGER.error("Failed to find map template at {}", path, e);
                    }

                    return this.buildFromTemplate(server, template);
                }
        );
    }

    private BwMap buildFromTemplate(MinecraftServer server, MapTemplate template) {
        BwMap map = new BwMap(config);

        MapTemplateMetadata metadata = template.getMetadata();
        metadata.getRegionBounds("diamond_spawn").forEach(map::addDiamondGenerator);
        metadata.getRegionBounds("emerald_spawn").forEach(map::addEmeraldGenerator);

        for (GameTeam team : this.config.teams()) {
            BwMap.TeamRegions regions = BwMap.TeamRegions.fromTemplate(team.key(), metadata);
            map.addTeamRegions(team.key(), regions, map.pools);
        }

        for (BlockPos pos : template.getBounds()) {
            BlockState state = template.getBlockState(pos);
            if (!state.isAir()) {
                map.addProtectedBlock(pos.asLong());
            }
        }

        metadata.getRegionBounds("illegal").forEach(map::addIllegalRegion);

        BlockBounds centerSpawnBounds = metadata.getFirstRegionBounds("center_spawn");
        if (centerSpawnBounds == null) {
            centerSpawnBounds = template.getBounds();
        }

        BlockPos centerSpawn = BlockPos.ofFloored(centerSpawnBounds.center());
        centerSpawn = template.getTopPos(centerSpawn.getX(), centerSpawn.getZ(), Heightmap.Type.WORLD_SURFACE).up();

        map.setCenterSpawn(centerSpawn);

        map.setChunkGenerator(new TemplateChunkGenerator(server, template));

        return map;
    }
}
