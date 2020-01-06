package net.snofox.minecraft.maimedmagic;

import com.google.common.collect.ImmutableSet;
import net.snofox.minecraft.snolib.MathExtras;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Josh on 2020-01-05
 * TODO: Re-roll instead of offering just Unbreaking. Requires NMS
 */
public class EnchantingListener implements Listener {

    private final MaimedMagic module;
    private final Map<UUID, EnchantmentOffer[]> offerCache;

    public EnchantingListener(final MaimedMagic module) {
        this.module = module;
        offerCache = new HashMap<>();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepareItemEnchant(PrepareItemEnchantEvent ev) {
        final EnchantmentOffer[] offers = ev.getOffers();
        offerCache.put(ev.getEnchanter().getUniqueId(), offers.clone());
        for(int i = 0; i < offers.length; ++i) {
            final EnchantmentOffer offer = offers[i];
            if(offer != null && !module.isAllowed(offer.getEnchantment())) {
                ev.getOffers()[i] = new EnchantmentOffer(
                        Enchantment.DURABILITY,
                        MathExtras.clamp(offers[i].getEnchantmentLevel(), 1, 3),
                        offers[i].getCost());
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onEnchantItem(final EnchantItemEvent ev) {
        final Map<Enchantment, Integer> enchantsToAdd = ev.getEnchantsToAdd();
        final ImmutableSet<Enchantment> enchants = ImmutableSet.copyOf(enchantsToAdd.keySet());
        module.debug("Enchants to apply: ");
        for(final Enchantment enchant : enchants) {
            module.debug("- " + enchant.getKey());
            if(module.isAllowed(enchant)) continue;
            enchantsToAdd.remove(enchant);
        }
        final EnchantmentOffer[] offers = offerCache.get(ev.getEnchanter().getUniqueId());
        final int button = ev.whichButton();
        if(offers == null) {
            module.getLogger().warning("Missed offer cache for " + ev.getEnchanter() + ". Enchant transaction will be wrong");
            return;
        } else if(button > offers.length) {
            module.getLogger().warning(ev.getEnchanter() + " enchanted with an out-of-bounds button. Enchant transaction will be wrong");
            return;
        } else if(offers[button] == null) {
            module.getLogger().warning(ev.getEnchanter() + " enchanted with a button that returned null. Enchant transaction will be wrong");
            return;
        }
        module.debug("Cached Offers: " );
        for(final EnchantmentOffer enchOffer : offers) {
            module.debug("- " + enchOffer.getEnchantment().getName() + " level " + enchOffer.getEnchantmentLevel());
        }
        module.debug("Selected: " + button);
        module.debug("Enchants applied: ");
        for(final Map.Entry<Enchantment, Integer> enchant : enchantsToAdd.entrySet())
            module.debug("- " + enchant.getKey().getKey() + " level " + enchant.getValue());
        if(!module.isAllowed(offers[button].getEnchantment())) {
            enchantsToAdd.put(Enchantment.DURABILITY, MathExtras.clamp(offers[button].getEnchantmentLevel(), 1, 3));
            module.debug("Also, added durability level" + offers[button].getEnchantmentLevel());
        }

    }
}
