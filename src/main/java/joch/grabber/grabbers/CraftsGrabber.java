package joch.grabber.grabbers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.Loader;
import joch.grabber.utils.Logger;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CraftsGrabber extends BaseGrabber {

    private static JsonObject HandleIC2IRecipe(Object recipe) {
        try {
            if (Class.forName("ic2.api.recipe.RecipeInputItemStack").isInstance(recipe)) {
                Class<?> _Class = Class.forName("ic2.api.recipe.RecipeInputItemStack");
                ItemStack inputStack = (ItemStack) _Class.getDeclaredField("input").get(recipe);
                inputStack.stackSize *= (int) _Class.getDeclaredMethod("getAmount").invoke(recipe);

                return itemToJSON(inputStack);
            } else if (Class.forName("ic2.api.recipe.RecipeInputOreDict").isInstance(recipe)) {
                Class<?> _Class = Class.forName("ic2.api.recipe.RecipeInputOreDict");
                JsonObject inputJSON = itemToJSON(OreDictionary.getOreID((String) _Class.getDeclaredField("input").get(recipe)));

                inputJSON.addProperty("qty", (int) _Class.getDeclaredMethod("getAmount").invoke(recipe));
                return inputJSON;

            } else if (Class.forName("ic2.api.recipe.RecipeInputFluidContainer").isInstance(recipe)) {
                Class<?> _Class = Class.forName("ic2.api.recipe.RecipeInputFluidContainer");
                Fluid _fluid = (Fluid) _Class.getDeclaredField("fluid").get(recipe);
                int amount = (int) _Class.getDeclaredMethod("getAmount").invoke(recipe);

                return itemToJSON(new FluidStack(_fluid, amount));

            } else if (recipe instanceof List<?>) {
                /*JsonArray array = new JsonArray();
                for( Object v : (List<?>) recipe ){
                    array.add( HandleIC2IRecipe( v ) );
                }
                System.out.println( array.toString() );
                return new JsonObject();*/

                return HandleIC2IRecipe(((List<?>) recipe).get(0));
            } else {
                return itemToJSON(recipe);
            }
        } catch (Exception e) {
            Logger.WriteLine("Crafts grabber, HandleIC2Recipe: \n\n\n\n" + e.toString());
        }

        return new JsonObject();
    }

    private static JsonArray HandleAppEndRecipe(Object item) {
        JsonArray response = new JsonArray();

        try {
            Class<?> _class = item.getClass();
            int qty = (int) _class.getDeclaredMethod("getQty").invoke(item);

            if ((boolean) _class.getDeclaredMethod("isAir").invoke(item)) {
                return response;
            }

            if (Class.forName("appeng.recipes.Ingredient").isInstance(item)) {
                JsonObject inputJson;
                if (((String) _class.getDeclaredMethod("getNameSpace").invoke(item)).equalsIgnoreCase("oreDictionary")) {
                    inputJson = itemToJSON(_class.getDeclaredMethod("getItemName").invoke(item));
                } else {
                    inputJson = itemToJSON(_class.getDeclaredMethod("getItemStack").invoke(item));
                }
                inputJson.addProperty("qty", qty);
                response.add(inputJson);

            } else if (Class.forName("appeng.recipes.GroupIngredient").isInstance(item)) {
                Field _field = _class.getDeclaredField("ingredients");
                _field.setAccessible(true);
                for (Object ingredient : (List<?>) _field.get(item)) {
                    response.addAll(HandleAppEndRecipe(ingredient));
                }

            } else if (Class.forName("appeng.recipes.IngredientSet").isInstance(item)) {
                String name = "ore" + _class.getDeclaredMethod("getItemName").invoke(item);

                int oreID = getGeneralOreID(Arrays.asList((ItemStack[]) _class.getDeclaredMethod("getItemStackSet").invoke(item)));
                if( oreID == -1 ){
                    if( !OreDictionary.doesOreNameExist( name ) ){
                        for (ItemStack input : (ItemStack[]) _class.getDeclaredMethod("getItemStackSet").invoke(item)) {
                            OreDictionary.registerOre(name, input);
                        }
                    }

                    oreID = OreDictionary.getOreID( name );
                }

                response.add(itemToJSON(oreID));
            }
        } catch (Exception e) {
        }

        return response;
    }

    private static boolean isIC2Recipe(IRecipe recipe) {
        if (!Loader.isModLoaded("IC2")) {
            return false;
        }

        try {
            return Class.forName("ic2.core.AdvRecipe").isInstance(recipe) || Class.forName("ic2.core.AdvShapelessRecipe").isInstance(recipe);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isAppEngRecipe(IRecipe recipe) {
        if (!Loader.isModLoaded("appliedenergistics2")) {
            return false;
        }

        try {
            return Class.forName("appeng.recipes.game.ShapedRecipe").isInstance(recipe) || Class.forName("appeng.recipes.game.ShapelessRecipe").isInstance(recipe);
        } catch (Exception e) {
            return false;
        }
    }

    public static JsonArray Grab() {
        JsonArray response = new JsonArray();

        for (IRecipe irecipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList()) {
            ItemStack item = irecipe.getRecipeOutput();
            if (item == null) continue;

            JsonObject recipe = new JsonObject();
            recipe.add("item", itemToJSON(item));

            JsonArray array = new JsonArray();
            try {
                if (irecipe instanceof ShapedRecipes) {
                    for (Object stack : ((ShapedRecipes) irecipe).recipeItems)
                        if (stack != null) array.add(itemToJSON(stack));

                } else if (irecipe instanceof ShapelessRecipes) {
                    for (Object stack : ((ShapelessRecipes) irecipe).recipeItems)
                        if (stack != null) array.add(itemToJSON(stack));

                } else if (irecipe instanceof ShapedOreRecipe) {
                    for (Object value : ((ShapedOreRecipe) irecipe).getInput())
                        if (value != null) array.add(itemToJSON(value));

                } else if (irecipe instanceof ShapelessOreRecipe) {
                    for (Object value : ((ShapelessOreRecipe) irecipe).getInput())
                        if (value != null) array.add(itemToJSON(value));

                } else if (isIC2Recipe(irecipe)) {
                    if (Class.forName("ic2.core.AdvRecipe").isInstance(irecipe)) {
                        for (Object value : (Object[]) Class.forName("ic2.core.AdvRecipe").getDeclaredField("input").get(irecipe))
                            if (value != null) array.add(HandleIC2IRecipe(value));

                    } else {
                        for (Object value : (Object[]) Class.forName("ic2.core.AdvShapelessRecipe").getDeclaredField("input").get(irecipe))
                            if (value != null) array.add(HandleIC2IRecipe(value));
                    }

                } else if (isAppEngRecipe(irecipe)) {
                    if (Class.forName("appeng.recipes.game.ShapedRecipe").isInstance(irecipe)) {
                        for (Object value : (Object[]) Class.forName("appeng.recipes.game.ShapedRecipe").getDeclaredMethod("getInput").invoke(irecipe))
                            if (value != null) array.addAll(HandleAppEndRecipe(value));

                    } else {
                        for (Object value : (ArrayList<Object>) Class.forName("appeng.recipes.game.ShapelessRecipe").getDeclaredMethod("getInput").invoke(irecipe))
                            if (value != null) array.addAll(HandleAppEndRecipe(value));
                    }

                }
            } catch (Exception e) {
                Logger.WriteLine("Craft grabber error:" + e.toString());
            }

            recipe.add("craft", array);
            response.add(recipe);
        }

        return response;
    }

}
