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
public class MultiwordPattern {

    String pos;
    String pattern;

    public MultiwordPattern(String input) {
        this.pos = "";
        this.pattern = "";
        String [] fields = input.split(":");
        if (fields.length==2) {
            pos = fields[0].trim().toLowerCase();
            pattern = fields[1].trim().toLowerCase();
        }
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
