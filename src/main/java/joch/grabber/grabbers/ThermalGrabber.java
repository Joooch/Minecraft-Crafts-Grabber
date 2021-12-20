package joch.grabber.grabbers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import joch.grabber.utils.Logger;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class ThermalGrabber extends BaseGrabber {

    private static int getEnergy(Object recipe) {
        try { // idk how to check if field exists. I'm new here
            return (int) recipe.getClass().getDeclaredMethod("getEnergy").invoke(recipe);
        } catch (Exception e) {
            Logger.WriteLine("\nGet Energy error: " + e);
        }

        return 0;
    }

    /*
        Recipe with fields:
            input: ItemStack
            primaryOutput: ItemStack
            secondaryOutput: ItemStack
            secondaryOutputChance: 0 < Int <= 100

            energy: int [optional]
     */
    private static void handleRecipeWithSecondaryAndPrimaryOutput(Object recipe, JsonArray outputArray) {
        try {
            Class<?> recipeClass = recipe.getClass();
            ItemStack input = (ItemStack) recipeClass.getDeclaredMethod("getInput").invoke(recipe);
            ItemStack primaryOutput = (ItemStack) recipeClass.getDeclaredMethod("getPrimaryOutput").invoke(recipe);
            ItemStack secondaryOutput = (ItemStack) recipeClass.getDeclaredMethod("getSecondaryOutput").invoke(recipe);
            int Energy = getEnergy(recipe);


            outputArray.add(createSimpleRecipeObject(input, primaryOutput, Energy));

            // If 100% chance of secondary output -> add second recipe
            if (secondaryOutput != null && (int) recipeClass.getDeclaredMethod("getSecondaryOutputChance").invoke(recipe) == 100) {
                outputArray.add(createSimpleRecipeObject(input, secondaryOutput, Energy));
            }
        } catch (Exception e) {
            Logger.WriteLine("\nThermal grabber error: handleRecipeWithSecondaryAndPrimaryOutput: " + e);
        }
    }


    /*
        Recipe with fields:
            primaryInput: ItemStack
            secondaryInput: ItemStack

            primaryOutput: ItemStack
            secondaryOutput: ItemStack

            secondaryOutputChance: 0 < Int <= 100

            energy: int [optional]
     */
    private static void handleRecipeWithTwoInputsAndOutputs(Object recipe, JsonArray outputArray) {
        try {
            Class<?> recipeClass = recipe.getClass();
            ItemStack primaryInput = (ItemStack) recipeClass.getDeclaredMethod("getPrimaryInput").invoke(recipe);
            ItemStack secondaryInput = (ItemStack) recipeClass.getDeclaredMethod("getSecondaryInput").invoke(recipe);
            ItemStack primaryOutput = (ItemStack) recipeClass.getDeclaredMethod("getPrimaryOutput").invoke(recipe);
            ItemStack secondaryOutput = (ItemStack) recipeClass.getDeclaredMethod("getSecondaryOutput").invoke(recipe);
            int Energy = getEnergy(recipe);


            outputArray.add(createRecipeObjectWithMultipleInputs(new ItemStack[]{primaryInput, secondaryInput}, primaryOutput, Energy));

            // If 100% chance of secondary output -> add second recipe
            if (secondaryOutput != null && (int) recipeClass.getDeclaredMethod("getSecondaryOutputChance").invoke(recipe) == 100) {
                outputArray.add(createRecipeObjectWithMultipleInputs(new ItemStack[]{primaryInput, secondaryInput}, secondaryOutput, Energy));
            }
        } catch (Exception e) {
            Logger.WriteLine("Thermal error: handleRecipeWithTwoInputsAndOutputs: " + e);
        }
    }


    /*
        Recipe with fields:
            input: ItemStack
            output: ItemStack

            energy: int [optional]
     */
    private static void handleRecipeWithOneOutput(Object recipe, JsonArray outputArray) {
        try {
            Class<?> recipeClass = recipe.getClass();
            ItemStack input = (ItemStack) recipeClass.getDeclaredMethod("getInput").invoke(recipe);
            ItemStack output = (ItemStack) recipeClass.getDeclaredMethod("getOutput").invoke(recipe);

            outputArray.add(createSimpleRecipeObject(input, output, getEnergy(recipe)));
        } catch (Exception e) {
            Logger.WriteLine("\nError Thermal: handleRecipeWithOneOutput: " + e);
        }
    }


    private static void handleRecipeTransposer(Object recipe, JsonArray outputArray) {
        try {
            Class<?> recipeClass = recipe.getClass();
            ItemStack input = (ItemStack) recipeClass.getDeclaredMethod("getInput").invoke(recipe);
            ItemStack output = (ItemStack) recipeClass.getDeclaredMethod("getOutput").invoke(recipe);
            FluidStack fluid = (FluidStack) recipeClass.getDeclaredMethod("getFluid").invoke(recipe);

            int energy = getEnergy(recipe);

            JsonObject recipeObject = new JsonObject();
            recipeObject.add("item", itemToJSON(output));

            JsonArray craftArray = new JsonArray();
            craftArray.add(itemToJSON(input));
            craftArray.add(itemToJSON(fluid));

            recipeObject.add("craft", craftArray);
            outputArray.add(recipeObject);
        } catch (Exception e) {
            Logger.WriteLine("\nError: handleRecipeTransposer: " + e);
        }
    }

    public static JsonArray Grab() {
        JsonArray response = new JsonArray();
        try {

            // RecipeCrucible[ ]
            for (Object recipe : (Object[]) Class.forName("cofh.thermalexpansion.util.crafting.CrucibleManager").getDeclaredMethod("getRecipeList").invoke(null)) {
                Class<?> recipeClass = recipe.getClass();
                ItemStack input = (ItemStack) recipeClass.getDeclaredMethod("getInput").invoke(recipe);
                FluidStack output = (FluidStack) recipeClass.getDeclaredMethod("getOutput").invoke(recipe);

                response.add(createSimpleRecipeObject(input, output, getEnergy(recipe)));
            }

            //RecipeTransposer
            for (Object recipe : (Object[]) Class.forName("cofh.thermalexpansion.util.crafting.TransposerManager").getDeclaredMethod("getFillRecipeList").invoke(null)) {
                handleRecipeTransposer(recipe, response);
            }

            // Water backet -> extract to Bucket + water.
            // Useless recipes. Ignore
            //for (Object recipe : (Object[]) Class.forName("cofh.thermalexpansion.util.crafting.TransposerManager").getDeclaredMethod("getExtractionRecipeList").invoke(null)) {
            //    handleRecipeTransposer( recipe, response );
            //}

            for (Object recipe : (Object[]) Class.forName("cofh.thermalexpansion.util.crafting.SawmillManager").getDeclaredMethod("getRecipeList").invoke(null)) {
                handleRecipeWithSecondaryAndPrimaryOutput(recipe, response);
            }

            // I'm lazy, ignore this
            // RecipeExtruder[ ] // water + lava = cobblestone, stone, obsidian.
            // RecipePrecipitator[ ] // water = snowball, snowblock, ice. Ignore

            // RecipeSawmill[ ]
            for (Object recipe : (Object[]) Class.forName("cofh.thermalexpansion.util.crafting.SawmillManager").getDeclaredMethod("getRecipeList").invoke(null)) {
                handleRecipeWithSecondaryAndPrimaryOutput(recipe, response);
            }

            // RecipeSmelter[ ]
            for (Object recipe : (Object[]) Class.forName("cofh.thermalexpansion.util.crafting.SmelterManager").getDeclaredMethod("getRecipeList").invoke(null)) {
                handleRecipeWithTwoInputsAndOutputs(recipe, response);
            }

            // RecipeInsolator[ ]
            for (Object recipe : (Object[]) Class.forName("cofh.thermalexpansion.util.crafting.InsolatorManager").getDeclaredMethod("getRecipeList").invoke(null)) {
                handleRecipeWithTwoInputsAndOutputs(recipe, response);
            }

            // RecipePulverizer[ ]
            for (Object recipe : (Object[]) Class.forName("cofh.thermalexpansion.util.crafting.PulverizerManager").getDeclaredMethod("getRecipeList").invoke(null)) {
                handleRecipeWithSecondaryAndPrimaryOutput(recipe, response);
            }

        } catch (Exception e) {
            Logger.WriteLine("\nError Thermal: main: " + e);
        }

        return response;
    }
}
