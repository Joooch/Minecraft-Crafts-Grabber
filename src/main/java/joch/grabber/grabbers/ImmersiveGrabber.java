package joch.grabber.grabbers;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonArray;
import joch.grabber.utils.Logger;

public class ImmersiveGrabber extends BaseGrabber {
    public static JsonArray Grab() {
        JsonArray response = new JsonArray();

        // Metal Press support::
        try {
            ArrayListMultimap<Object, Object> recipesList = (ArrayListMultimap<Object, Object>) Class.forName("blusunrize.immersiveengineering.api.crafting.MetalPressRecipe").getDeclaredField("recipeList").get(null);

            // Ignore "Metal press Mold" for crafting recipe
            for (Object recipe : recipesList.values()) {
                Class<?> _class = recipe.getClass();

                Object input = _class.getDeclaredField("input").get(recipe);
                Object output = _class.getDeclaredField("output").get(recipe);
                int energy = (int) _class.getDeclaredField("energy").get(recipe);

                response.add(createSimpleRecipeObject(input, output, energy));
            }
        } catch (Exception e) {
            Logger.WriteLine("Immersive error: " + e);
        }

        return response;
    }
}
