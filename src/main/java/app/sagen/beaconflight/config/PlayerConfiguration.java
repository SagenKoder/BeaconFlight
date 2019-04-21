/******************************************************************************
 * Copyright (C) BlueLapiz.net - All Rights Reserved                          *
 * Unauthorized copying of this file, via any medium is strictly prohibited   *
 * Proprietary and confidential                                               *
 * Last edited 11/26/18 2:40 PM                                               *
 * Written by Alexander Sagen <alexmsagen@gmail.com>                          *
 ******************************************************************************/

package app.sagen.beaconflight.config;

import java.util.UUID;

public interface PlayerConfiguration extends GeneralConfiguration {

    boolean createFile();

    void discard();

    void discard(boolean save);

    boolean exists();

    void forceSave();

    UUID getManagerPlayerUUID();

    boolean isFirstJoin();

    void setFirstJoin(boolean firstJoin);
}
