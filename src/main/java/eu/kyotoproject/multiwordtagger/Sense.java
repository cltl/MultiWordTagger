package eu.kyotoproject.multiwordtagger;

/**
 * Created by IntelliJ IDEA.
 * User: Piek Vossen
 * Date: aug-2010
 * Time: 6:34:44
 * To change this template use File | Settings | File Templates.
 * This file is part of KafMultiWordtagger.

 KafMultiWordtagger is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 KafMultiWordtagger is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with KafMultiWordtagger.  If not, see <http://www.gnu.org/licenses/>.
 */
public class Sense {

    private String sense_key;
    private String synsetId;

    public Sense() {
        this.sense_key = "";
        this.synsetId = "";
    }

    public String getSense_key() {
        return sense_key;
    }

    public void setSense_key(String sense_key) {
        this.sense_key = sense_key;
    }

    public String getSynsetId() {
        return synsetId;
    }

    public void setSynsetId(String synsetId) {
        this.synsetId = synsetId;
    }
}
