package net.snofox.minecraft.maimedmagic;

import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.snofox.minecraft.snolib.language.ItemNames;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Josh on 2020-01-04
 */
public class MaimedMagic extends JavaPlugin {
    private static MaimedMagic instance;

    private boolean whitelist;
    private ImmutableList<Enchantment> enchantments;
    private ImmutableList<PotionType> potionEffects;

    @Contract(pure = true)
    public static MaimedMagic getInstance() {
        return getInstance();
    }

    @Override
    public void onLoad() {
       instance = this;
       saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        reloadConfig();
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getLogger().info("Magic is feeling weaker...");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        final List<Enchantment> newEnchants = new ArrayList<>();
        for(final String enchantStr : getConfig().getStringList("enchants")) {
            final Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(enchantStr));
            if(enchant == null) getLogger().warning("Unable to find enchantment key " + enchantStr);
            else newEnchants.add(enchant);
        }
        enchantments = ImmutableList.copyOf(newEnchants);
        final List<PotionType> newPotionEffects = new ArrayList<>();
        for(final String potionStr : getConfig().getStringList("potions")) {
            try {
                final PotionType potionEffect = PotionType.valueOf(potionStr);
                newPotionEffects.add(potionEffect);
            } catch(IllegalArgumentException ex) {
                getLogger().warning("Unable to find potion named " + potionStr);
            }
        }
        newPotionEffects.add(PotionType.AWKWARD);
        newPotionEffects.add(PotionType.MUNDANE);
        newPotionEffects.add(PotionType.THICK);
        potionEffects = ImmutableList.copyOf(newPotionEffects);
        whitelist = getConfig().getBoolean("whitelistMode", true);
    }

    public boolean isAllowed(final Enchantment enchantment) {
        return enchantments.contains(enchantment) && whitelist;
    }

    public boolean isAllowed(final PotionType potionEffect) {
        return potionEffects.contains(potionEffect) && whitelist;
    }

    /* pkg-private */ void notifyPlayer(final Player player, final String message) {
        try {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
        } catch(NoSuchMethodError ex) {
            player.sendMessage(ChatColor.RED + message);
        }
    }
}
