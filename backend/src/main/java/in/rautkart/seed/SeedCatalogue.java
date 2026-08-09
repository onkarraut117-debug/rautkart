package in.rautkart.seed;

import java.util.ArrayList;
import java.util.List;

/**
 * The demo grocery catalogue. Prices are rough Indian retail prices so the
 * storefront looks like a real neighbourhood kirana shop rather than lorem ipsum.
 *
 * Columns: name, description, categorySlug, price, mrp, unit, stock, emoji
 */
final class SeedCatalogue {

    private SeedCatalogue() {
    }

    static List<Object[]> rows() {
        List<Object[]> rows = new ArrayList<>();
        staples(rows);
        fresh(rows);
        packaged(rows);
        return rows;
    }

    private static void staples(List<Object[]> rows) {
        // --- Grains & Rice -------------------------------------------------
        rows.add(new Object[]{"India Gate Basmati Rice", "Long-grain aged basmati, stays separate after cooking. Great for biryani and pulao.", "grains-rice", "189.00", "225.00", "1 kg", 60, "🍚"});
        rows.add(new Object[]{"Sona Masoori Rice", "Everyday soft-cooking rice from Andhra. Light and easy to digest.", "grains-rice", "72.00", "85.00", "1 kg", 120, "🍚"});
        rows.add(new Object[]{"Aashirvaad Whole Wheat Atta", "Chakki-fresh atta milled from sharbati wheat. Makes soft phulkas.", "grains-rice", "245.00", "285.00", "5 kg", 45, "🌾"});
        rows.add(new Object[]{"Bansi Rava (Sooji)", "Coarse semolina for upma, halwa and rava dosa.", "grains-rice", "48.00", "55.00", "1 kg", 80, "🌾"});
        rows.add(new Object[]{"Poha (Thick Beaten Rice)", "Thick variety that holds shape - the right one for kanda poha.", "grains-rice", "42.00", "50.00", "500 g", 70, "🥣"});
        rows.add(new Object[]{"Besan (Gram Flour)", "Stone-ground chana dal flour for pakoras, kadhi and laddoos.", "grains-rice", "68.00", "80.00", "1 kg", 55, "🌾"});

        // --- Dals & Pulses -------------------------------------------------
        rows.add(new Object[]{"Toor Dal (Arhar)", "Unpolished pigeon pea, the base of everyday varan and sambar.", "dals-pulses", "148.00", "175.00", "1 kg", 90, "🫘"});
        rows.add(new Object[]{"Moong Dal (Split Yellow)", "Quick-cooking and light. Good for khichdi and dal fry.", "dals-pulses", "124.00", "145.00", "1 kg", 75, "🫘"});
        rows.add(new Object[]{"Chana Dal", "Split Bengal gram with a nutty bite. Also used for dal vada.", "dals-pulses", "98.00", "115.00", "1 kg", 85, "🫘"});
        rows.add(new Object[]{"Masoor Dal (Red Lentil)", "Cooks in under 20 minutes, no soaking needed.", "dals-pulses", "112.00", "130.00", "1 kg", 65, "🫘"});
        rows.add(new Object[]{"Urad Dal (Black Gram)", "For idli, dosa batter and dal makhani.", "dals-pulses", "136.00", "160.00", "1 kg", 50, "🫘"});
        rows.add(new Object[]{"Kabuli Chana (Chickpeas)", "Plump white chana for chole and salads.", "dals-pulses", "105.00", "125.00", "1 kg", 60, "🫛"});
        rows.add(new Object[]{"Rajma (Kidney Beans)", "Jammu-style dark red rajma, soft after an overnight soak.", "dals-pulses", "142.00", "165.00", "1 kg", 40, "🫘"});

        // --- Oils & Ghee ---------------------------------------------------
        rows.add(new Object[]{"Fortune Sunflower Oil", "Light refined sunflower oil for daily cooking.", "oils-ghee", "158.00", "180.00", "1 L", 100, "🫗"});
        rows.add(new Object[]{"Cold Pressed Groundnut Oil", "Wood-pressed and unrefined. Strong aroma, the traditional Maharashtrian choice.", "oils-ghee", "285.00", "320.00", "1 L", 35, "🥜"});
        rows.add(new Object[]{"Amul Pure Ghee", "Cow ghee with a rich grainy texture.", "oils-ghee", "610.00", "660.00", "1 L", 30, "🧈"});
        rows.add(new Object[]{"Mustard Oil (Kachi Ghani)", "Pungent cold-pressed mustard oil for pickles and Bengali cooking.", "oils-ghee", "175.00", "199.00", "1 L", 40, "🌻"});

        // --- Spices & Masala -----------------------------------------------
        rows.add(new Object[]{"Turmeric Powder (Haldi)", "Single-origin Salem turmeric, deep colour and high curcumin.", "spices-masala", "58.00", "70.00", "200 g", 110, "🟡"});
        rows.add(new Object[]{"Red Chilli Powder", "Kashmiri blend - bright colour, moderate heat.", "spices-masala", "82.00", "95.00", "200 g", 95, "🌶️"});
        rows.add(new Object[]{"Coriander Powder (Dhania)", "Freshly ground dhania with a citrusy finish.", "spices-masala", "54.00", "65.00", "200 g", 90, "🌿"});
        rows.add(new Object[]{"Everest Garam Masala", "Classic warm-spice blend for curries and dals.", "spices-masala", "76.00", "85.00", "100 g", 70, "🥄"});
        rows.add(new Object[]{"Cumin Seeds (Jeera)", "Whole jeera for tempering. Clean, no stalks.", "spices-masala", "94.00", "110.00", "200 g", 80, "🫙"});
        rows.add(new Object[]{"Mustard Seeds (Rai)", "Small black mustard seeds that pop fast in hot oil.", "spices-masala", "36.00", "45.00", "200 g", 100, "🫙"});
        rows.add(new Object[]{"Tata Salt Iodised", "Free-flowing iodised salt.", "spices-masala", "28.00", "30.00", "1 kg", 150, "🧂"});
    }

    private static void fresh(List<Object[]> rows) {
        // --- Dairy & Eggs --------------------------------------------------
        rows.add(new Object[]{"Amul Taaza Toned Milk", "Homogenised toned milk in a tetra pack. No refrigeration until opened.", "dairy-eggs", "78.00", "82.00", "1 L", 60, "🥛"});
        rows.add(new Object[]{"Amul Butter Salted", "The yellow block that goes on everything.", "dairy-eggs", "62.00", "66.00", "100 g", 90, "🧈"});
        rows.add(new Object[]{"Paneer (Fresh)", "Soft malai paneer, cut to order. Made the same morning.", "dairy-eggs", "96.00", "110.00", "200 g", 25, "🧀"});
        rows.add(new Object[]{"Amul Masti Dahi", "Thick set curd, mildly sour.", "dairy-eggs", "36.00", "40.00", "400 g", 45, "🥛"});
        rows.add(new Object[]{"Farm Eggs", "Free-range brown eggs from a farm outside the city.", "dairy-eggs", "84.00", "95.00", "6 pcs", 55, "🥚"});
        rows.add(new Object[]{"Amul Cheese Slices", "Processed cheese slices for sandwiches and toasties.", "dairy-eggs", "135.00", "150.00", "200 g", 30, "🧀"});

        // --- Fruits & Vegetables -------------------------------------------
        rows.add(new Object[]{"Onion", "Nashik onions, medium size. Sold loose.", "fruits-vegetables", "32.00", "40.00", "1 kg", 200, "🧅"});
        rows.add(new Object[]{"Tomato", "Firm hybrid tomatoes, good for gravy.", "fruits-vegetables", "28.00", "35.00", "1 kg", 180, "🍅"});
        rows.add(new Object[]{"Potato", "All-purpose potatoes for sabzi and frying.", "fruits-vegetables", "26.00", "32.00", "1 kg", 220, "🥔"});
        rows.add(new Object[]{"Coriander Leaves (Kothimbir)", "Fresh bunch with roots on, picked this morning.", "fruits-vegetables", "12.00", "15.00", "1 bunch", 60, "🌿"});
        rows.add(new Object[]{"Green Chilli", "Medium-hot local chillies.", "fruits-vegetables", "18.00", "24.00", "250 g", 70, "🌶️"});
        rows.add(new Object[]{"Banana (Robusta)", "Ripe and ready to eat.", "fruits-vegetables", "48.00", "60.00", "1 dozen", 40, "🍌"});
        rows.add(new Object[]{"Apple (Shimla)", "Crisp red apples from Himachal.", "fruits-vegetables", "168.00", "199.00", "1 kg", 35, "🍎"});
        rows.add(new Object[]{"Lemon", "Juicy thin-skinned lemons.", "fruits-vegetables", "40.00", "50.00", "250 g", 50, "🍋"});
        rows.add(new Object[]{"Ginger", "Fresh adrak, low fibre.", "fruits-vegetables", "56.00", "70.00", "250 g", 45, "🫚"});
        rows.add(new Object[]{"Garlic", "Local variety with large, easy-to-peel cloves.", "fruits-vegetables", "88.00", "105.00", "500 g", 40, "🧄"});
    }

    private static void packaged(List<Object[]> rows) {
        // --- Snacks & Namkeen ----------------------------------------------
        rows.add(new Object[]{"Haldiram Aloo Bhujia", "Crisp potato sev with a peppery finish.", "snacks-namkeen", "52.00", "60.00", "200 g", 85, "🥨"});
        rows.add(new Object[]{"Parle-G Biscuits", "The glucose biscuit everyone grew up on.", "snacks-namkeen", "30.00", "35.00", "800 g", 100, "🍪"});
        rows.add(new Object[]{"Lays Classic Salted", "Thin-cut potato chips.", "snacks-namkeen", "20.00", "20.00", "52 g", 120, "🥔"});
        rows.add(new Object[]{"Roasted Peanuts (Masala)", "Roasted and tossed in chaat masala.", "snacks-namkeen", "45.00", "55.00", "250 g", 60, "🥜"});
        rows.add(new Object[]{"Good Day Cashew Cookies", "Buttery cookies with cashew bits.", "snacks-namkeen", "45.00", "50.00", "200 g", 75, "🍪"});
        rows.add(new Object[]{"Chivda (Bhajani)", "Home-style Maharashtrian poha chivda, lightly sweet and spicy.", "snacks-namkeen", "88.00", "100.00", "400 g", 30, "🥗"});

        // --- Beverages -----------------------------------------------------
        rows.add(new Object[]{"Tata Tea Gold", "Assam CTC blend with long leaf. Makes strong kadak chai.", "beverages", "265.00", "295.00", "500 g", 55, "☕"});
        rows.add(new Object[]{"Bru Instant Coffee", "Chicory blend instant coffee.", "beverages", "185.00", "210.00", "100 g", 40, "☕"});
        rows.add(new Object[]{"Real Mixed Fruit Juice", "No added preservatives, one litre pack.", "beverages", "115.00", "130.00", "1 L", 45, "🧃"});
        rows.add(new Object[]{"Bisleri Mineral Water", "Sealed one litre bottle.", "beverages", "20.00", "20.00", "1 L", 150, "💧"});
        rows.add(new Object[]{"Rooh Afza Sharbat", "Rose sherbet concentrate for summer drinks.", "beverages", "185.00", "200.00", "750 ml", 25, "🌹"});

        // --- Bakery --------------------------------------------------------
        rows.add(new Object[]{"Brown Bread", "Whole wheat loaf, baked daily.", "bakery", "45.00", "50.00", "400 g", 30, "🍞"});
        rows.add(new Object[]{"Pav (Ladi)", "Soft ladi pav, 8 pieces. For vada pav and misal.", "bakery", "32.00", "35.00", "8 pcs", 40, "🍞"});
        rows.add(new Object[]{"Britannia Rusk", "Crunchy elaichi rusk toast for chai.", "bakery", "55.00", "62.00", "300 g", 50, "🍞"});

        // --- Household & Cleaning ------------------------------------------
        rows.add(new Object[]{"Surf Excel Easy Wash", "Detergent powder for hand and machine wash.", "household-cleaning", "185.00", "210.00", "1 kg", 60, "🧴"});
        rows.add(new Object[]{"Vim Dishwash Bar", "Lemon dishwash bar, cuts grease.", "household-cleaning", "30.00", "35.00", "300 g", 90, "🧼"});
        rows.add(new Object[]{"Harpic Toilet Cleaner", "Thick liquid cleaner, 500 ml.", "household-cleaning", "95.00", "110.00", "500 ml", 45, "🧴"});
        rows.add(new Object[]{"Lifebuoy Soap", "Pack of 4 bathing bars.", "household-cleaning", "132.00", "150.00", "4 x 125 g", 55, "🧼"});
        rows.add(new Object[]{"Garbage Bags (Medium)", "Roll of 30 biodegradable bags.", "household-cleaning", "78.00", "90.00", "30 pcs", 35, "🗑️"});
    }
}
