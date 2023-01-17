package com.github.dcysteine.nesql.server.plugin.base;

import com.github.dcysteine.nesql.server.plugin.base.display.recipe.DisplayRecipe;
import com.github.dcysteine.nesql.server.plugin.base.display.BaseDisplayService;
import com.github.dcysteine.nesql.server.plugin.base.spec.RecipeSpec;
import com.github.dcysteine.nesql.server.service.SearchService;
import com.github.dcysteine.nesql.sql.base.recipe.Recipe;
import com.github.dcysteine.nesql.sql.base.recipe.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

@Controller
@RequestMapping(path = "/recipe")
public class RecipeController {
    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private BaseDisplayService baseDisplayService;

    @Autowired
    private SearchService searchService;

    @GetMapping(path = "/view/{recipe_id}")
    public String view(@PathVariable(name = "recipe_id") String id, Model model) {
        Optional<Recipe> recipeOptional = recipeRepository.findById(id);
        if (recipeOptional.isEmpty()) {
            return "not_found";
        }
        Recipe recipe = recipeOptional.get();
        DisplayRecipe displayRecipe = baseDisplayService.buildDisplayRecipe(recipe);

        model.addAttribute("recipe", recipe);
        model.addAttribute("displayRecipe", displayRecipe);
        return "plugin/base/recipe/view";
    }

    @GetMapping(path = "/search")
    public String search() {
        return "plugin/base/recipe/search";
    }

    @GetMapping(path = "/all")
    public String all(@RequestParam(defaultValue = "1") int page, Model model) {
        return searchService.handleGetAll(
                page, model, recipeRepository, baseDisplayService::buildDisplayRecipeIcon);
    }

    @GetMapping(path = "/searchresults")
    public String searchResults(
            @RequestParam(required = false) Optional<String> inputItemId,
            @RequestParam(defaultValue = "1") int page,
            Model model) {
        @Nullable
        Specification<Recipe> inputItemIdSpec =
                inputItemId
                        .filter(Predicate.not(String::isEmpty))
                        .map(RecipeSpec::buildInputItemSpec).orElse(null);

        Specification<Recipe> spec = Specification.allOf(inputItemIdSpec);
        return searchService.handleSearch(
                page, model, recipeRepository,
                spec, baseDisplayService::buildDisplayRecipeIcon);
    }
}
