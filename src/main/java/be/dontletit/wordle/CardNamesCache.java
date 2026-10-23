package be.dontletit.wordle;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.function.Supplier;

/**
 * Holds the reference lists (card names, first-printing sets) in memory so the front end
 * can be served without hitting MTGODatabase (whose list endpoints are slow) on every page
 * load. An empty upstream list is not cached so an import in progress does not pin an
 * empty result.
 */
@ApplicationScoped
public class CardNamesCache {

    @Inject
    @RestClient
    MtgoDatabaseClient database;

    private final CachedList<String> names = new CachedList<>();
    private final CachedList<SetDto> sets = new CachedList<>();

    public List<String> get() {
        return names.get(database::cardNames);
    }

    public List<SetDto> sets() {
        return sets.get(database::firstPrintingSets);
    }

    public void clear() {
        names.clear();
        sets.clear();
    }

    private static final class CachedList<T> {
        private volatile List<T> value;

        List<T> get(Supplier<List<T>> loader) {
            List<T> cached = value;
            if (cached != null) return cached;
            synchronized (this) {
                if (value == null) {
                    List<T> fetched = List.copyOf(loader.get());
                    if (fetched.isEmpty()) return fetched;
                    value = fetched;
                }
                return value;
            }
        }

        synchronized void clear() {
            value = null;
        }
    }
}
