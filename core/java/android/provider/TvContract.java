/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.provider;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.tv.TvInputService;

import java.util.List;

/**
 * <p>
 * The contract between the TV provider and applications. Contains definitions for the supported
 * URIs and columns.
 * </p>
 * <h3>Overview</h3>
 * <p>
 * TvContract defines a basic database of TV content metadata such as channel and program
 * information. The information is stored in {@link Channels} and {@link Programs} tables.
 * </p>
 * <ul>
 *     <li>A row in the {@link Channels} table represents information about a TV channel. The data
 *         format can vary greatly from standard to standard or according to service provider, thus
 *         the columns here are mostly comprised of basic entities that are usually seen to users
 *         regardless of standard such as channel number and name.</li>
 *     <li>A row in the {@link Programs} table represents a set of data describing a TV program such
 *         as program title and start time.</li>
 * </ul>
 */
public final class TvContract {
    /** The authority for the TV provider. */
    public static final String AUTHORITY = "com.android.tv";

    private static final String PATH_CHANNEL = "channel";
    private static final String PATH_PROGRAM = "program";
    private static final String PATH_INPUT = "input";

    /**
     * An optional query, update or delete URI parameter that allows the caller to specify start
     * time (in milliseconds since the epoch) to filter programs.
     *
     * @hide
     */
    public static final String PARAM_START_TIME = "start_time";

    /**
     * An optional query, update or delete URI parameter that allows the caller to specify end time
     * (in milliseconds since the epoch) to filter programs.
     *
     * @hide
     */
    public static final String PARAM_END_TIME = "end_time";

    /**
     * A query, update or delete URI parameter that allows the caller to operate on all or
     * browsable-only channels. If set to "true", the rows that contain non-browsable channels are
     * not affected.
     *
     * @hide
     */
    public static final String PARAM_BROWSABLE_ONLY = "browable_only";

    /**
     * Builds a URI that points to a specific channel.
     *
     * @param channelId The ID of the channel to point to.
     */
    public static final Uri buildChannelUri(long channelId) {
        return ContentUris.withAppendedId(Channels.CONTENT_URI, channelId);
    }

    /**
     * Builds a URI that points to all browsable channels from a given TV input.
     *
     * @param name {@link ComponentName} of the {@link android.tv.TvInputService} that implements
     *            the given TV input.
     */
    public static final Uri buildChannelsUriForInput(ComponentName name) {
        return buildChannelsUriForInput(name, true);
    }

    /**
     * Builds a URI that points to all or browsable-only channels from a given TV input.
     *
     * @param name {@link ComponentName} of the {@link android.tv.TvInputService} that implements
     *            the given TV input.
     * @param browsableOnly If set to {@code true} the URI points to only browsable channels. If set
     *            to {@code false} the URI points to all channels regardless of whether they are
     *            browsable or not.
     */
    public static final Uri buildChannelsUriForInput(ComponentName name, boolean browsableOnly) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY)
                .appendPath(PATH_INPUT).appendPath(name.getPackageName())
                .appendPath(name.getClassName()).appendPath(PATH_CHANNEL)
                .appendQueryParameter(PARAM_BROWSABLE_ONLY, String.valueOf(browsableOnly)).build();
    }

    /**
     * Builds a URI that points to a specific program.
     *
     * @param programId The ID of the program to point to.
     */
    public static final Uri buildProgramUri(long programId) {
        return ContentUris.withAppendedId(Programs.CONTENT_URI, programId);
    }

    /**
     * Builds a URI that points to all programs on a given channel.
     *
     * @param channelUri The URI of the channel to return programs for.
     */
    public static final Uri buildProgramsUriForChannel(Uri channelUri) {
        if (!PATH_CHANNEL.equals(channelUri.getPathSegments().get(0))) {
            throw new IllegalArgumentException("Not a channel: " + channelUri);
        }
        String channelId = String.valueOf(ContentUris.parseId(channelUri));
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY)
                .appendPath(PATH_CHANNEL).appendPath(channelId).appendPath(PATH_PROGRAM).build();
    }

    /**
     * Builds a URI that points to programs on a specific channel whose schedules overlap with the
     * given time frame.
     *
     * @param channelUri The URI of the channel to return programs for.
     * @param startTime The start time used to filter programs. The returned programs should have
     *            {@link Programs#COLUMN_END_TIME_UTC_MILLIS} that is greater than this time.
     * @param endTime The end time used to filter programs. The returned programs should have
     *            {@link Programs#COLUMN_START_TIME_UTC_MILLIS} that is less than this time.
     */
    public static final Uri buildProgramsUriForChannel(Uri channelUri, long startTime,
            long endTime) {
        Uri uri = buildProgramsUriForChannel(channelUri);
        return uri.buildUpon().appendQueryParameter(PARAM_START_TIME, String.valueOf(startTime))
                .appendQueryParameter(PARAM_END_TIME, String.valueOf(endTime)).build();
    }

    /**
     * Builds a URI that points to a specific program the user watched.
     *
     * @param watchedProgramId The ID of the watched program to point to.
     * @hide
     */
    public static final Uri buildWatchedProgramUri(long watchedProgramId) {
        return ContentUris.withAppendedId(WatchedPrograms.CONTENT_URI, watchedProgramId);
    }

    /**
     * Extracts the {@link Channels#COLUMN_PACKAGE_NAME} from a given URI.
     *
     * @param channelsUri A URI constructed by {@link #buildChannelsUriForInput(ComponentName)} or
     *            {@link #buildChannelsUriForInput(ComponentName, boolean)}.
     * @hide
     */
    public static final String getPackageName(Uri channelsUri) {
        final List<String> paths = channelsUri.getPathSegments();
        if (paths.size() < 4) {
            throw new IllegalArgumentException("Not channels: " + channelsUri);
        }
        if (!PATH_INPUT.equals(paths.get(0)) || !PATH_CHANNEL.equals(paths.get(3))) {
            throw new IllegalArgumentException("Not channels: " + channelsUri);
        }
        return paths.get(1);
    }

    /**
     * Extracts the {@link Channels#COLUMN_SERVICE_NAME} from a given URI.
     *
     * @param channelsUri A URI constructed by {@link #buildChannelsUriForInput(ComponentName)} or
     *            {@link #buildChannelsUriForInput(ComponentName, boolean)}.
     * @hide
     */
    public static final String getServiceName(Uri channelsUri) {
        final List<String> paths = channelsUri.getPathSegments();
        if (paths.size() < 4) {
            throw new IllegalArgumentException("Not channels: " + channelsUri);
        }
        if (!PATH_INPUT.equals(paths.get(0)) || !PATH_CHANNEL.equals(paths.get(3))) {
            throw new IllegalArgumentException("Not channels: " + channelsUri);
        }
        return paths.get(2);
    }

    /**
     * Extracts the {@link Channels#_ID} from a given URI.
     *
     * @param programsUri A URI constructed by {@link #buildProgramsUriForChannel(Uri)} or
     *            {@link #buildProgramsUriForChannel(Uri, long, long)}.
     * @hide
     */
    public static final String getChannelId(Uri programsUri) {
        final List<String> paths = programsUri.getPathSegments();
        if (paths.size() < 3) {
            throw new IllegalArgumentException("Not programs: " + programsUri);
        }
        if (!PATH_CHANNEL.equals(paths.get(0)) || !PATH_PROGRAM.equals(paths.get(2))) {
            throw new IllegalArgumentException("Not programs: " + programsUri);
        }
        return paths.get(1);
    }


    private TvContract() {}

    /**
     * Common base for the tables of TV channels/programs.
     */
    public interface BaseTvColumns extends BaseColumns {
        /**
         * The name of the package that owns a row in each table.
         * <p>
         * The TV provider fills it in with the name of the package that provides the initial data
         * of that row. If the package is later uninstalled, the rows it owns are automatically
         * removed from the tables.
         * </p><p>
         * Type: TEXT
         * </p>
         */
        public static final String COLUMN_PACKAGE_NAME = "package_name";
    }

    /** Column definitions for the TV channels table. */
    public static final class Channels implements BaseTvColumns {

        /** The content:// style URI for this table. */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
                + PATH_CHANNEL);

        /** The MIME type of a directory of TV channels. */
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.com.android.tv.channels";

        /** The MIME type of a single TV channel. */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.com.android.tv.channels";

        /** A generic channel type. */
        public static final int TYPE_OTHER = 0x0;

        /** The special channel type used for pass-through inputs such as HDMI. */
        public static final int TYPE_PASSTHROUGH = 0x00010000;

        /** The channel type for DVB-T (terrestrial). */
        public static final int TYPE_DVB_T = 0x00020000;

        /** The channel type for DVB-T2 (terrestrial). */
        public static final int TYPE_DVB_T2 = 0x00020001;

        /** The channel type for DVB-S (satellite). */
        public static final int TYPE_DVB_S = 0x00020100;

        /** The channel type for DVB-S2 (satellite). */
        public static final int TYPE_DVB_S2 = 0x00020101;

        /** The channel type for DVB-C (cable). */
        public static final int TYPE_DVB_C = 0x00020200;

        /** The channel type for DVB-C2 (cable). */
        public static final int TYPE_DVB_C2 = 0x00020201;

        /** The channel type for DVB-H (handheld). */
        public static final int TYPE_DVB_H = 0x00020300;

        /** The channel type for DVB-SH (satellite). */
        public static final int TYPE_DVB_SH = 0x00020400;

        /** The channel type for ATSC (terrestrial). */
        public static final int TYPE_ATSC_T = 0x00030000;

        /** The channel type for ATSC (cable). */
        public static final int TYPE_ATSC_C = 0x00030200;

        /** The channel type for ATSC-M/H (mobile/handheld). */
        public static final int TYPE_ATSC_M_H = 0x00030200;

        /** The channel type for ISDB-T (terrestrial). */
        public static final int TYPE_ISDB_T = 0x00040000;

        /** The channel type for ISDB-Tb (Brazil). */
        public static final int TYPE_ISDB_TB = 0x00040100;

        /** The channel type for ISDB-S (satellite). */
        public static final int TYPE_ISDB_S = 0x00040200;

        /** The channel type for ISDB-C (cable). */
        public static final int TYPE_ISDB_C = 0x00040300;

        /** The channel type for 1seg (handheld). */
        public static final int TYPE_1SEG = 0x00040400;

        /** The channel type for DTMB (terrestrial). */
        public static final int TYPE_DTMB = 0x00050000;

        /** The channel type for CMMB (handheld). */
        public static final int TYPE_CMMB = 0x00050100;

        /** The channel type for T-DMB (terrestrial). */
        public static final int TYPE_T_DMB = 0x00060000;

        /** The channel type for S-DMB (satellite). */
        public static final int TYPE_S_DMB = 0x00060100;

        /** A generic service type. */
        public static final int SERVICE_TYPE_OTHER = 0x0;

        /** The service type for regular TV channels. */
        public static final int SERVICE_TYPE_TV = 0x1;

        /** The service type for radio channels. */
        public static final int SERVICE_TYPE_RADIO = 0x2;

        /**
         * The name of the {@link TvInputService} subclass that provides this TV channel. This
         * should be a fully qualified class name (such as, "com.example.project.TvInputService").
         * <p>
         * This is a required field.
         * </p><p>
         * Type: TEXT
         * </p>
         */
        public static final String COLUMN_SERVICE_NAME = "service_name";

        /**
         * The predefined type of this TV channel.
         * <p>
         * This is primarily used to indicate which broadcast standard (e.g. ATSC, DVB or ISDB) the
         * current channel conforms to, with an exception being {@link #TYPE_PASSTHROUGH}, which is
         * a special channel type used only by pass-through inputs such as HDMI. The value should
         * match to one of the followings: {@link #TYPE_OTHER}, {@link #TYPE_PASSTHROUGH},
         * {@link #TYPE_DVB_T}, {@link #TYPE_DVB_T2}, {@link #TYPE_DVB_S}, {@link #TYPE_DVB_S2},
         * {@link #TYPE_DVB_C}, {@link #TYPE_DVB_C2}, {@link #TYPE_DVB_H}, {@link #TYPE_DVB_SH},
         * {@link #TYPE_ATSC_T}, {@link #TYPE_ATSC_C}, {@link #TYPE_ATSC_M_H}, {@link #TYPE_ISDB_T},
         * {@link #TYPE_ISDB_TB}, {@link #TYPE_ISDB_S}, {@link #TYPE_ISDB_C} {@link #TYPE_1SEG},
         * {@link #TYPE_DTMB}, {@link #TYPE_CMMB}, {@link #TYPE_T_DMB}, {@link #TYPE_S_DMB}
         * </p><p>
         * This is a required field.
         * </p><p>
         * Type: INTEGER
         * </p>
         */
        public static final String COLUMN_TYPE = "type";

        /**
         * The predefined service type of this TV channel.
         * <p>
         * This is primarily used to indicate whether the current channel is a regular TV channel or
         * a radio-like channel. Use the same coding for {@code service_type} in the underlying
         * broadcast standard if it is defined there (e.g. ATSC A/53, ETSI EN 300 468 and ARIB
         * STD-B10). Otherwise use one of the followings: {@link #SERVICE_TYPE_OTHER},
         * {@link #SERVICE_TYPE_TV}, {@link #SERVICE_TYPE_RADIO}
         * </p><p>
         * This is a required field.
         * </p><p>
         * Type: INTEGER
         * </p>
         */
        public static final String COLUMN_SERVICE_TYPE = "service_type";

        /**
         * The original network ID of this TV channel.
         * <p>
         * This is used to identify the originating delivery system, if applicable. Use the same
         * coding for {@code origianal_network_id} in the underlying broadcast standard if it is
         * defined there (e.g. ETSI EN 300 468/TR 101 211 and ARIB STD-B10). If channels cannot be
         * globally identified by 2-tuple {{@link #COLUMN_TRANSPORT_STREAM_ID},
         * {@link #COLUMN_SERVICE_ID}}, one must carefully assign a value to this field to form a
         * unique 3-tuple identification {{@link #COLUMN_ORIGINAL_NETWORK_ID},
         * {@link #COLUMN_TRANSPORT_STREAM_ID}, {@link #COLUMN_SERVICE_ID}} for its channels.
         * </p><p>
         * This is a required field if the channel cannot be uniquely identified by a 2-tuple
         * {{@link #COLUMN_TRANSPORT_STREAM_ID}, {@link #COLUMN_SERVICE_ID}}.
         * </p><p>
         * Type: INTEGER
         * </p>
         */
        public static final String COLUMN_ORIGINAL_NETWORK_ID = "original_network_id";

        /**
         * The transport stream ID of this channel.
         * <p>
         * This is used to identify the Transport Stream that contains the current channel from any
         * other multiplex within a network, if applicable. Use the same coding for
         * {@code transport_stream_id} defined in ISO/IEC 13818-1 if the channel is transmitted via
         * the MPEG Transport Stream as is the case for many digital broadcast standards.
         * </p><p>
         * This is a required field if the current channel is transmitted via the MPEG Transport
         * Stream.
         * </p><p>
         * Type: INTEGER
         * </p>
         */
        public static final String COLUMN_TRANSPORT_STREAM_ID = "transport_stream_id";

        /**
         * The service ID of this channel.
         * <p>
         * This is used to identify the current service (roughly equivalent to channel) from any
         * other service within the Transport Stream, if applicable. Use the same coding for
         * {@code service_id} in the underlying broadcast standard if it is defined there (e.g. ETSI
         * EN 300 468 and ARIB STD-B10) or {@code program_number} (which usually has the same value
         * as {@code service_id}) in ISO/IEC 13818-1 if the channel is transmitted via the MPEG
         * Transport Stream.
         * </p><p>
         * This is a required field if the current channel is transmitted via the MPEG Transport
         * Stream.
         * </p><p>
         * Type: INTEGER
         * </p>
         */
        public static final String COLUMN_SERVICE_ID = "service_id";

        /**
         * The channel number that is displayed to the user.
         * <p>
         * The format can vary depending on broadcast standard and product specification.
         * </p><p>
         * Type: TEXT
         * </p>
         */
        public static final String COLUMN_DISPLAY_NUMBER = "display_number";

        /**
         * The channel name that is displayed to the user.
         * <p>
         * A call sign is a good candidate to use for this purpose but any name that helps the user
         * recognize the current channel will be enough. Can also be empty depending on broadcast
         * standard.
         * </p><p>
         * Type: TEXT
         * </p>
         */
        public static final String COLUMN_DISPLAY_NAME = "display_name";

        /**
         * The description of this TV channel.
         * <p>
         * Can be empty initially.
         * </p><p>
         * Type: TEXT
         * </p>
         */
        public static final String COLUMN_DESCRIPTION = "description";

        /**
         * The flag indicating whether this TV channel is browsable or not.
         * <p>
         * A value of 1 indicates the channel is included in the channel list that applications use
         * to browse channels, a value of 0 indicates the channel is not included in the list. If
         * not specified, this value is set to 1 by default.
         * </p><p>
         * Type: INTEGER (boolean)
         * </p>
         */
        public static final String COLUMN_BROWSABLE = "browsable";

        /**
         * Generic data used by individual TV input services.
         * <p>
         * Type: BLOB
         * </p>
         */
        public static final String COLUMN_DATA = "data";


        /**
         * The version number of this row entry used by TV input services.
         * <p>
         * This is best used by sync adapters to identify the rows to update. The number can be
         * defined by individual TV input services. One may assign the same value as
         * {@code version_number} that appears in ETSI EN 300 468 or ATSC A/65, if the data are
         * coming from a TV broadcast.
         * </p><p>
         * Type: INTEGER
         * </p>
         */
        public static final String COLUMN_VERSION_NUMBER = "version_number";

        private Channels() {}
    }

    /** Column definitions for the TV programs table. */
    public static final class Programs implements BaseTvColumns {

        /** The content:// style URI for this table. */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"
                + PATH_PROGRAM);

        /** The MIME type of a directory of TV programs. */
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.com.android.tv.programs";

        /** The MIME type of a single TV program. */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.com.android.tv.programs";

        /**
         * The ID of the TV channel that contains this TV program.
         * <p>
         * This is a part of the channel URI and matches to {@link BaseColumns#_ID}.
         * </p><p>
         * Type: INTEGER (long)
         * </p>
         */
        public static final String COLUMN_CHANNEL_ID = "channel_id";

        /**
         * The title of this TV program.
         * <p>
         * Type: TEXT
         * </p>
         **/
        public static final String COLUMN_TITLE = "title";

        /**
         * The start time of this TV program, in milliseconds since the epoch.
         * <p>
         * Type: INTEGER (long)
         * </p>
         */
        public static final String COLUMN_START_TIME_UTC_MILLIS = "start_time_utc_millis";

        /**
         * The end time of this TV program, in milliseconds since the epoch.
         * <p>
         * Type: INTEGER (long)
         * </p>
         */
        public static final String COLUMN_END_TIME_UTC_MILLIS = "end_time_utc_millis";

        /**
         * The description of this TV program that is displayed to the user by default.
         * <p>
         * The maximum length of this field is 256 characters.
         * </p><p>
         * Type: TEXT
         * </p>
         */
        public static final String COLUMN_DESCRIPTION = "description";

        /**
         * The detailed, lengthy description of this TV program that is displayed only when the user
         * wants to see more information.
         * <p>
         * TV input services should leave this field empty if they have no additional
         * details beyond {@link #COLUMN_DESCRIPTION}.
         * </p><p>
         * Type: TEXT
         * </p>
         */
        public static final String COLUMN_LONG_DESCRIPTION = "long_description";

        /**
         * Generic data used by TV input services.
         * <p>
         * Type: BLOB
         * </p>
         */
        public static final String COLUMN_DATA = "data";

        /**
         * The version number of this row entry used by TV input services.
         * <p>
         * This is best used by sync adapters to identify the rows to update. The number can be
         * defined by individual TV input services. One may assign the same value as
         * {@code version_number} in ETSI EN 300 468 or ATSC A/65, if the data are coming from a TV
         * broadcast.
         * </p><p>
         * Type: INTEGER
         * </p>
         */
        public static final String COLUMN_VERSION_NUMBER = "version_number";

        private Programs() {}
    }

    /**
     * Column definitions for the TV programs that the user watched. Applications do not have access
     * to this table.
     *
     * @hide
     */
    public static final class WatchedPrograms implements BaseColumns {

        /** The content:// style URI for this table. */
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + AUTHORITY + "/watched_program");

        /** The MIME type of a directory of watched programs. */
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.com.android.tv.watched_programs";

        /** The MIME type of a single item in this table. */
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.com.android.tv.watched_programs";

        /**
         * The UTC time that the user started watching this TV program, in milliseconds since the
         * epoch.
         * <p>
         * Type: INTEGER (long)
         * </p>
         */
        public static final String COLUMN_WATCH_START_TIME_UTC_MILLIS =
                "watch_start_time_utc_millis";

        /**
         * The UTC time that the user stopped watching this TV program, in milliseconds since the
         * epoch.
         * <p>
         * Type: INTEGER (long)
         * </p>
         */
        public static final String COLUMN_WATCH_END_TIME_UTC_MILLIS = "watch_end_time_utc_millis";

        /**
         * The channel ID that contains this TV program.
         * <p>
         * Type: INTEGER (long)
         * </p>
         */
        public static final String COLUMN_CHANNEL_ID = "channel_id";

        /**
         * The title of this TV program.
         * <p>
         * Type: TEXT
         * </p>
         */
        public static final String COLUMN_TITLE = "title";

        /**
         * The start time of this TV program, in milliseconds since the epoch.
         * <p>
         * Type: INTEGER (long)
         * </p>
         */
        public static final String COLUMN_START_TIME_UTC_MILLIS = "start_time_utc_millis";

        /**
         * The end time of this TV program, in milliseconds since the epoch.
         * <p>
         * Type: INTEGER (long)
         * </p>
         */
        public static final String COLUMN_END_TIME_UTC_MILLIS = "end_time_utc_millis";

        /**
         * The description of this TV program.
         * <p>
         * Type: TEXT
         * </p>
         */
        public static final String COLUMN_DESCRIPTION = "description";

        private WatchedPrograms() {}
    }
}