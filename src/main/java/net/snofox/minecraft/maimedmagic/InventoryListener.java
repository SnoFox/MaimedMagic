package net.snofox.minecraft.maimedmagic;

import net.snofox.minecraft.snolib.RandomUtil;
import net.snofox.minecraft.snolib.language.ItemNames;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Map;

/**
 * Created by Josh on 2020-01-04
 */
public class InventoryListener implements Listener {
    private final MaimedMagic module;

    public InventoryListener(final MaimedMagic module) {
        this.module = module;
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemSpawn(final ItemSpawnEvent ev) {
        handleItem(ev.getEntity());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPickupItem(final EntityPickupItemEvent ev) {
        handleItem(ev.getItem());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepareItemEnchant(PrepareItemEnchantEvent ev) {
        // TODO: Re-roll instead of offering nothing
        final EnchantmentOffer[] offers = ev.getOffers();
        for(int i = 0; i < offers.length; ++i) {
            final EnchantmentOffer offer = offers[i];
            if(offer != null && !module.isAllowed(offer.getEnchantment())) ev.getOffers()[i] = null;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent ev) {
        // Examine Brewing Stand inventories, since BrewEvent fires before the result appears for cleaning
        if(!(ev.getClickedInventory() instanceof BrewerInventory)) return;
        final ItemStack currentItem = ev.getCurrentItem();
        if(handleItemStack(currentItem)) {
            ev.setCurrentItem(currentItem);
            if(ev.getWhoClicked() instanceof Player) {
                final Player p = (Player) ev.getWhoClicked();
                module.notifyPlayer(p, "The magic in the "
                        + ItemNames.getItemLocalizedName(currentItem) + " dispelled upon your touch");
                if(ev.getClick().equals(ClickType.LEFT) || ev.getClick().equals(ClickType.RIGHT)) return;
                p.updateInventory();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(final InventoryOpenEvent ev) {
        if(ev.getViewers().size() != 1) return;
        handleInventory(ev.getView().getTopInventory());
    }

    private boolean handleInventory(final Inventory inventory) {
        boolean dirty = false;
        final ItemStack[] storageContents = inventory.getStorageContents();
        for(int i = 0; i < storageContents.length; ++i) {
            if(handleItemStack(storageContents[i])) {
                inventory.getStorageContents()[i] = storageContents[i];
                dirty = true;
            }
        }
        return dirty;
    }

    private boolean handleItemStack(final ItemStack item) {
        return handleEnchants(item) || handlePotions(item);
    }

    private boolean handleItem(final Item item) {
        final ItemStack itemStack = item.getItemStack();
        final boolean dirty = handleEnchants(itemStack) || handlePotions(itemStack);
        if(dirty) item.setItemStack(itemStack);
        return dirty;
    }

    private boolean handleEnchants(final ItemStack itemStack) {
        if(itemStack == null) return false;
        boolean dirty = false;
        for(final Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {
            final Enchantment enchant = entry.getKey();
            if(!module.isAllowed(enchant)) {
                itemStack.removeEnchantment(enchant);
                dirty = true;
            }
        }
        return dirty;
    }

    private boolean handlePotions(final ItemStack itemStack) {
        // TODO: Add support for custom potions
        if(!isPotion(itemStack) || !itemStack.hasItemMeta()) return false;
        final PotionMeta itemMeta = (PotionMeta)itemStack.getItemMeta();
        final PotionData basePotionData = itemMeta.getBasePotionData();
        boolean dirty = false;
        if(!module.isAllowed(basePotionData.getType())) {
            dirty = true;
            itemMeta.setBasePotionData(new PotionData(randomBasePotion(),false, false));
        }
        if(dirty) itemStack.setItemMeta(itemMeta);
        return dirty;
    }

    private PotionType randomBasePotion() {
        switch(RandomUtil.getRandom().nextInt(3)) {
            case 0:
                return PotionType.AWKWARD;
            case 1:
                return PotionType.MUNDANE;
            case 2:
                return PotionType.THICK;
            default:
                return PotionType.LUCK; // lucky you, the random number generator broke
        }
    }

    private boolean isPotion(final ItemStack itemStack) {
        if(itemStack == null) return false;
        switch(itemStack.getType()) {
            case POTION:
            case LINGERING_POTION:
            case SPLASH_POTION:
            case TIPPED_ARROW:
                return true;
            default:
                return false;
        }
    }
}
