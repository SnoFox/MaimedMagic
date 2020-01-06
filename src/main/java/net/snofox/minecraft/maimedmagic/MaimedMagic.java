package net.snofox.minecraft.maimedmagic;

import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Contract;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Josh on 2020-01-04
 */
public class MaimedMagic extends JavaPlugin {

    private boolean whitelist;
    private ImmutableSet<Enchantment> enchantments;
    private ImmutableSet<PotionType> potionEffects;

    @Contract(pure = true)
    public static MaimedMagic getInstance() {
        return getInstance();
    }

    @Override
    public void onLoad() {
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        reloadConfig();
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchantingListener(this), this);
        getLogger().info("Magic is feeling weaker...");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        final Set<Enchantment> newEnchants = new HashSet<>();
        for(final String enchantStr : getConfig().getStringList("enchants")) {
            final Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(enchantStr));
            if(enchant == null) getLogger().warning("Unable to find enchantment key " + enchantStr);
            else newEnchants.add(enchant);
        }
        newEnchants.add(Enchantment.DURABILITY);
        enchantments = ImmutableSet.copyOf(newEnchants);
        final Set<PotionType> newPotionEffects = new HashSet<>();
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
        potionEffects = ImmutableSet.copyOf(newPotionEffects);
        whitelist = getConfig().getBoolean("whitelistMode", true);
    }

    /* pkg-private */ void debug(final String message) {
        if(getConfig().getBoolean("debug", false)) getLogger().info("[Debug] " + message);
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
