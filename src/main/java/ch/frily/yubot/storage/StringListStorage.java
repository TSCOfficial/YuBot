package ch.frily.yubot.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores a list of text values per key
 * <p>
 * Deliberately not generic: the composite looks its storages up by class and a {@code ListStorage<E>}
 * would lose {@code E} to erasure on the way out. If you need lists of another element type, add a
 * sibling class for it.
 */
public class StringListStorage extends Storage<List<String>> {

}
