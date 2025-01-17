package dev.ftb.mods.ftboceanmobs;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.ftb.mods.ftboceanmobs.entity.riftweaver.RiftWeaverBoss;
import dev.ftb.mods.ftboceanmobs.entity.riftweaver.RiftWeaverModes;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ModCommands {
    public static final SimpleCommandExceptionType NO_WEAVER
            = new SimpleCommandExceptionType(Component.literal("No Rift Weaver within 100 blocks"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignoredBuildContext) {
        dispatcher.register(literal(FTBOceanMobs.MODID)
                .then(literal("weavertest")
                        .requires(cs -> cs.hasPermission(2))
                        .then(literal("mode")
                                .then(argument("modename", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(RiftWeaverModes.sortedNames(), builder))
                                        .executes(ctx -> setWeaverMode(ctx, StringArgumentType.getString(ctx, "modename")))
                                )
                        )
                        .then(literal("toggle_armor")
                                .executes(ModCommands::toggleWeaverArmor)
                        )
                        .then(literal("toggle_frenzy")
                                .executes(ModCommands::toggleWeaverFrenzy)
                        )
                        .then(literal("mark_arena")
                                .executes(ModCommands::markArena)
                        )
                )
        );
    }

    private static int setWeaverMode(CommandContext<CommandSourceStack> ctx, String modename) throws CommandSyntaxException {
        try {
            findWeaver(ctx).forceQueueMode(RiftWeaverModes.byName(modename).orElseThrow(IllegalArgumentException::new));
            ctx.getSource().sendSuccess(() -> Component.literal("Set mode for Rift Weaver to '" + modename + "'"), false);
            return Command.SINGLE_SUCCESS;
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal("Invalid mode '" + modename + "'").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int toggleWeaverArmor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        RiftWeaverBoss weaver = findWeaver(ctx);
        weaver.setArmorActive(!weaver.isArmorActive());
        return Command.SINGLE_SUCCESS;
    }

    private static int toggleWeaverFrenzy(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        RiftWeaverBoss weaver = findWeaver(ctx);
        weaver.setFrenzied(!weaver.isFrenzied());
        return Command.SINGLE_SUCCESS;
    }

    private static int markArena(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        RiftWeaverBoss weaver = findWeaver(ctx);

        for (int i = -Config.arenaRadius; i <= Config.arenaRadius; i++) {
            for (int j = -Config.arenaRadius; j <= Config.arenaRadius; j++) {
                int x = weaver.getSpawnPos().getX() + i;
                int z = weaver.getSpawnPos().getZ() + j;
                int y = weaver.level().getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                long d = Math.round(weaver.getSpawnPos().distToCenterSqr(x, weaver.getSpawnPos().getY(), z));
                if (Math.abs(d - Config.arenaRadiusSq) < 48) {
                    for (int k = 0; k < 5; k++) {
                        weaver.level().setBlock(new BlockPos(x, y + k, z), Blocks.GOLD_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                    }
                } else if (d < Config.arenaRadiusSq - 48) {
                    if (weaver.level().random.nextInt(100) == 0) {
                        weaver.level().setBlock(new BlockPos(x, y, z), Blocks.SPONGE.defaultBlockState(), Block.UPDATE_ALL);
                    } else {
                        weaver.level().setBlock(new BlockPos(x, y, z), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
        weaver.level().setBlock(weaver.getSpawnPos().above(), Blocks.COPPER_BLOCK.defaultBlockState(), Block.UPDATE_ALL);

        return Command.SINGLE_SUCCESS;
    }

    private static RiftWeaverBoss findWeaver(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        RiftWeaverBoss boss = player.level().getNearestEntity(RiftWeaverBoss.class, TargetingConditions.DEFAULT,
                player, player.getX(), player.getY(), player.getZ(), new AABB(player.blockPosition()).inflate(100));
        if (boss == null) {
            throw NO_WEAVER.create();
        } else {
            return boss;
        }
    }
}
