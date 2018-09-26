package io.vertigo.ui.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.vertigo.dynamo.domain.model.DtListState;
import io.vertigo.ui.core.ViewContext;
import io.vertigo.ui.core.ViewContextKey;
import io.vertigo.ui.domain.movies.Movie;
import io.vertigo.ui.impl.springmvc.argumentresolvers.ViewAttribute;
import io.vertigo.ui.impl.springmvc.controller.AbstractVSpringMvcController;
import io.vertigo.ui.services.movies.MovieServices;

@Controller
@RequestMapping("/movie")
public class MovieController extends AbstractVSpringMvcController {

	private static final ViewContextKey<Movie> movieKey = ViewContextKey.of("movie");
	private static final ViewContextKey<Movie> moviesKey = ViewContextKey.of("movies");

	@Autowired
	private MovieServices movieServices;

	@GetMapping("/")
	public void initContext(final ViewContext viewContext, @RequestParam("movId") final Long movId) {
		final Movie movie = movieServices.get(movId);
		viewContext.publishDto(movieKey, movie);
		viewContext.publishDtList(moviesKey, movieServices.getMovies(new DtListState(200, 0, null, null)));
	}

	@PostMapping("/_edit")
	public void doEdit() {
		toModeEdit();
	}

	@PostMapping("/_save")
	public String doSave(
			@ViewAttribute("movie") final Movie movie, final RedirectAttributes redirectAttributes) {
		movieServices.save(movie);
		redirectAttributes.addAttribute("movId", movie.getMovId());
		return "redirect:/movie/";
	}

}