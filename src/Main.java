import controller.LibraryController;
import model.LibraryData;
import util.Constants;
import view.ConsoleView;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Application entry point.
 * Loads data (Model), creates the View and Controller, then starts the event loop.
 */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        String dataDirectory = args.length == 0 ? Constants.DEFAULT_DATA_DIR : args[0];
        ConsoleView view = new ConsoleView();

        LibraryData data = new LibraryData();
        try {
            data.loadAndReplace(Paths.get(dataDirectory));
        } catch (IOException | IllegalArgumentException ex) {
            view.showLoadError(ex.getMessage());
            return;
        }

        LibraryData.State state = data.getCurrent();
        view.showLoadSuccess(state.getBooks().size(), state.getBranchIds().size());

        LibraryController controller = new LibraryController(state, view);
        controller.run();
    }
}
