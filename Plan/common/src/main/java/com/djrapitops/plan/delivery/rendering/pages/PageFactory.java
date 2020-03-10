/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.rendering.pages;

import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.rendering.html.icon.Icon;
import com.djrapitops.plan.delivery.web.resolver.exception.NotFoundException;
import com.djrapitops.plan.extension.implementation.results.ExtensionData;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionPlayerDataQuery;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.version.VersionChecker;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.debug.DebugLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;

/**
 * Factory for creating different {@link Page} objects.
 *
 * @author Rsl1122
 */
@Singleton
public class PageFactory {

    private final Lazy<VersionChecker> versionChecker;
    private final Lazy<PlanFiles> files;
    private final Lazy<PlanConfig> config;
    private final Lazy<Locale> locale;
    private final Lazy<Theme> theme;
    private final Lazy<DBSystem> dbSystem;
    private final Lazy<ServerInfo> serverInfo;
    private final Lazy<Formatters> formatters;
    private final Lazy<DebugLogger> debugLogger;
    private final Lazy<Timings> timings;
    private final Lazy<ErrorHandler> errorHandler;

    @Inject
    public PageFactory(
            Lazy<VersionChecker> versionChecker,
            Lazy<PlanFiles> files,
            Lazy<PlanConfig> config,
            Lazy<Locale> locale,
            Lazy<Theme> theme,
            Lazy<DBSystem> dbSystem,
            Lazy<ServerInfo> serverInfo,
            Lazy<Formatters> formatters,
            Lazy<DebugLogger> debugLogger,
            Lazy<Timings> timings,
            Lazy<ErrorHandler> errorHandler
    ) {
        this.versionChecker = versionChecker;
        this.files = files;
        this.config = config;
        this.locale = locale;
        this.theme = theme;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.formatters = formatters;
        this.debugLogger = debugLogger;
        this.timings = timings;
        this.errorHandler = errorHandler;
    }

    public DebugPage debugPage() throws IOException {
        return new DebugPage(
                getResource("web/error.html"),
                dbSystem.get().getDatabase(), serverInfo.get(), formatters.get(), versionChecker.get(),
                debugLogger.get(), timings.get(), errorHandler.get()
        );
    }

    public PlayersPage playersPage() throws IOException {
        return new PlayersPage(getResource("web/players.html"), versionChecker.get(),
                config.get(), locale.get(), theme.get(), serverInfo.get());
    }

    /**
     * Create a server page.
     *
     * @param serverUUID UUID of the server
     * @return {@link Page} that matches the server page.
     * @throws NotFoundException If the server can not be found in the database.
     * @throws IOException       If the template files can not be read.
     */
    public Page serverPage(UUID serverUUID) throws IOException {
        Server server = dbSystem.get().getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverUUID))
                .orElseThrow(() -> new NotFoundException("Server not found in the database"));
        return new ServerPage(
                getResource("web/server.html"),
                server,
                config.get(),
                theme.get(),
                locale.get(),
                versionChecker.get(),
                dbSystem.get(),
                serverInfo.get(),
                formatters.get()
        );
    }

    public PlayerPage playerPage(UUID playerUUID) throws IOException {
        Database db = dbSystem.get().getDatabase();
        PlayerContainer player = db.query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));
        return new PlayerPage(
                getResource("web/player.html"), player,
                versionChecker.get(),
                config.get(), this, theme.get(), locale.get(),
                formatters.get(), serverInfo.get()
        );
    }

    public PlayerPluginTab inspectPluginTabs(UUID playerUUID) {
        Database database = dbSystem.get().getDatabase();

        Map<UUID, List<ExtensionData>> extensionPlayerData = database.query(new ExtensionPlayerDataQuery(playerUUID));

        if (extensionPlayerData.isEmpty()) {
            return new PlayerPluginTab("", Collections.emptyList(), formatters.get());
        }

        List<PlayerPluginTab> playerPluginTabs = new ArrayList<>();
        for (Map.Entry<UUID, Server> entry : database.query(ServerQueries.fetchPlanServerInformation()).entrySet()) {
            UUID serverUUID = entry.getKey();
            String serverName = entry.getValue().getIdentifiableName();

            List<ExtensionData> ofServer = extensionPlayerData.get(serverUUID);
            if (ofServer == null) {
                continue;
            }

            playerPluginTabs.add(new PlayerPluginTab(serverName, ofServer, formatters.get()));
        }

        StringBuilder navs = new StringBuilder();
        StringBuilder tabs = new StringBuilder();

        playerPluginTabs.stream().sorted().forEach(tab -> {
            navs.append(tab.getNav());
            tabs.append(tab.getTab());
        });

        return new PlayerPluginTab(navs.toString(), tabs.toString());
    }

    public NetworkPage networkPage() throws IOException {
        return new NetworkPage(getResource("web/network.html"),
                dbSystem.get(),
                versionChecker.get(),
                config.get(), theme.get(), locale.get(),
                serverInfo.get(), formatters.get());
    }

    public Page internalErrorPage(String message, Throwable error) {
        try {
            return new InternalErrorPage(
                    getResource("web/error.html"), message, error,
                    versionChecker.get());
        } catch (IOException noParse) {
            return () -> "Error occurred: " + error.toString() +
                    ", additional error occurred when attempting to render error page to user: " +
                    noParse.toString();
        }
    }

    public Page errorPage(String title, String error) throws IOException {
        return new ErrorMessagePage(
                getResource("web/error.html"), title, error,
                versionChecker.get(), locale.get(), theme.get());
    }

    public Page errorPage(Icon icon, String title, String error) throws IOException {
        return new ErrorMessagePage(
                getResource("web/error.html"), icon, title, error,
                locale.get(), theme.get(), versionChecker.get());
    }

    public String getResource(String name) throws IOException {
        return files.get().getCustomizableResourceOrDefault(name).asString();
    }
}