// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.dto;

import java.util.ArrayList;

public class NOIPlaceDto {

    private ArrayList<NOIPlaceData> data;

    public ArrayList<NOIPlaceData> getData() {
        return data;
    }

    public void setData(ArrayList<NOIPlaceData> data) {
        this.data = data;
    }
}