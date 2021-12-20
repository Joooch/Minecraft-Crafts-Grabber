package joch.grabber.grabbers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import joch.grabber.utils.Logger;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class BaseGrabber {
    public static JsonArray Grab() {
        return null;
    }

    protected static JsonObject createRecipeObjectWithMultipleInputs(Object[] inputs, ItemStack output) {
        // Create simple recipe json object
        JsonObject response = new JsonObject();
        response.add("item", itemToJSON(output));

        // Create craft Array
        JsonArray craft = new JsonArray();
        for (Object input : inputs) {
            if (input == null) continue;

            JsonObject t = itemToJSON(input);
            craft.add(t);
            response.add("craft", craft);
        }

        return response;
    }

    protected static JsonObject createRecipeObjectWithMultipleInputs(Object[] inputs, ItemStack output, int energyRF) {
        JsonObject response = createRecipeObjectWithMultipleInputs(inputs, output);
        response.addProperty("energy", energyRF);

        return response;
    }


    // One input, One output
    protected static JsonObject createSimpleRecipeObject(Object input, Object output) {
        // Create simple recipe json object
        JsonObject response = new JsonObject();
        response.add("item", itemToJSON(output));

        // Create craft Array
        JsonArray craft = new JsonArray();
        craft.add(itemToJSON(input));
        response.add("craft", craft);

        return response;
    }

    protected static JsonObject createSimpleRecipeObject(Object input, Object output, int energyRF) {
        JsonObject response = createSimpleRecipeObject(input, output);
        response.addProperty("energy", energyRF);

        return response;
    }


    protected static int getGeneralOreID(List<ItemStack> items) {
        String[] names = OreDictionary.getOreNames();
        for (int i = 0; i < names.length; i++)
            if (OreDictionary.getOres(names[i], false).equals(items))
                return i;

        return -1;
    }

    protected static JsonObject itemToJSON(Object item) {
        if (item == null) {
            return null;
        }
        if (item instanceof List) {
            int id = getGeneralOreID((List) item);
            if (id == -1) {
                return itemToJSON(((List) item).get(0));
            } else {
                return itemToJSON(id);
            }

        } else if (item instanceof FluidStack) {
            JsonObject response = new JsonObject();
            FluidStack _stack = (FluidStack) item;
            Fluid _fluid = _stack.getFluid();

            response.addProperty("id", "fluid");
            response.addProperty("dmg", _stack.getFluidID());
            response.addProperty("qty", _stack.amount);
            response.addProperty("display_name", _fluid.getName());

            response.addProperty("isFluid", true);
            return response;

        } else if (item instanceof ItemStack) {
            JsonObject response = new JsonObject();
            ItemStack stack = (ItemStack) item;

            response.addProperty("id", stack.getItem().delegate.name());

            int dmg = stack.getItemDamage();
            if (dmg >= OreDictionary.WILDCARD_VALUE) dmg = 0;

            response.addProperty("dmg", dmg);

            if (stack.stackSize != 1)
                response.addProperty("qty", stack.stackSize);

            try {
                response.addProperty("display_name", stack.getDisplayName());
            } catch (Exception e) {
                response.addProperty("display_name", "Unknown");
            }
            if (stack.isItemStackDamageable() && !stack.getItemUseAction( ).equals( EnumAction.block )){
                response.addProperty("isDamagable", true);
            }

            if( stack.hasTagCompound( ) )
                response.addProperty("nbt", stack.getTagCompound( ).toString());

            return response;

        } else if (item instanceof Integer && (int) item != -1) {
            JsonObject response = new JsonObject();

            response.addProperty("id", "oredictionary");
            response.addProperty("dmg", (int) item);
            response.addProperty("qty", 1);
            response.addProperty("display_name", OreDictionary.getOreName((int) item));

            response.addProperty("isDictionary", true);

            return response;
        } else if (item instanceof String) {
            return itemToJSON(OreDictionary.getOreID((String) item));
        }

        Logger.WriteLine("not found, itemToJSON class for :" + item.getClass().toString() + " = " + item.toString());
        for (Field f : item.getClass().getDeclaredFields()){
            Logger.WriteLine("\t" + f.getName() + " : " + f.toString());
        }
        for (Method f : item.getClass().getDeclaredMethods()){
            Logger.WriteLine("\t" + f.getName() + " : " + f.toString());
        }
        return new JsonObject();
    }
}
