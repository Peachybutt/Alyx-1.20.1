package net.peachybutt.AlyxAwakened.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.peachybutt.AlyxAwakened.AlyxAwakened;
import net.peachybutt.AlyxAwakened.entity.ModEntities;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AlyxAwakened.MOD_ID);

    public static final RegistryObject<Item> INSERTNAME = ITEMS.register("insertname",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> UNOBTANIUM = ITEMS.register("unobtanium",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ALYX_SPAWN_EGG = ITEMS.register("alyx_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ALYX, 0xCEC5AD, 0xF2C2B0,
                    new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
