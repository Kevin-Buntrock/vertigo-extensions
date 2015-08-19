package io.vertigo.addons.impl.comment;

import io.vertigo.addons.comment.Comment;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.lang.Plugin;

import java.util.List;
import java.util.UUID;

/**
 * @author pchretien
 */
public interface CommentPlugin extends Plugin {

	<S extends KeyConcept> void publish(Comment comment, URI<S> keyConceptURI);

	Comment get(UUID uuid);

	<S extends KeyConcept> List<Comment> getComments(URI<S> keyConceptURI);

	void update(Comment comment);

}
