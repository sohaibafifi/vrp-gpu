/*
 *   Copyright or © or Copr. LGI2A
 *
 *   LGI2A - Laboratoire de Genie Informatique et d'Automatique de l'Artois - EA 3926
 *   Faculte des Sciences Appliquees
 *   Technoparc Futura
 *   62400 - BETHUNE Cedex
 *   http://www.lgi2a.univ-artois.fr/
 *
 *   Email: sohaib.lafifi@univ-artois.fr
 *
 *   Contributors:
 *   	Sohaib LAFIFI
 *
 *   This software is a computer program whose purpose is to implement some
 *   routing algorithms on GPU.
 *
 *   This software is governed by the CeCILL-B license under French law and
 *   abiding by the rules of distribution of free software.  You can  use,
 *   modify and/ or redistribute the software under the terms of the CeCILL-B
 *   license as circulated by CEA, CNRS and INRIA at the following URL
 *   "http://www.cecill.info".
 *
 *   As a counterpart to the access to the source code and  rights to copy,
 *   modify and redistribute granted by the license, users are provided only
 *   with a limited warranty  and the software's author,  the holder of the
 *   economic rights,  and the successive licensors  have only  limited
 *   liability.
 *
 *   In this respect, the user's attention is drawn to the risks associated
 *   with loading,  using,  modifying and/or developing or reproducing the
 *   software by the user in light of its specific status of free software,
 *   that may mean  that it is complicated to manipulate,  and  that  also
 *   therefore means  that it is reserved for developers  and  experienced
 *   professionals having in-depth computer knowledge. Users are therefore
 *   encouraged to load and test the software's suitability as regards their
 *   requirements in conditions enabling the security of their systems and/or
 *   data to be ensured and,  more generally, to use and operate it in the
 *   same conditions as regards security.
 *
 *   The fact that you are presently reading this means that you have had
 *   knowledge of the CeCILL-B license and that you accept its terms.
 *
 *
 */

package fr.univ_artois.lgi2a.vrpgpu.data;

import fr.univ_artois.lgi2a.vrpgpu.data.types.Coordinate;
import fr.univ_artois.lgi2a.vrpgpu.data.types.TimeWindow;

import java.util.Objects;

public class Client {
    private final int id;
    private float demand;
    private final TimeWindow timeWindow;
    private final float serviceTime;
    private final Coordinate coordinate;

    public Client(int id, Coordinate coordinate, float demand, TimeWindow timeWindow, float serviceTime) {
        this.id = id;
        this.coordinate = coordinate;
        this.demand = demand;
        this.timeWindow = timeWindow;
        this.serviceTime = serviceTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Float.compare(client.getDemand(), getDemand()) == 0 &&
                Float.compare(client.getServiceTime(), getServiceTime()) == 0 &&
                Objects.equals(getTimeWindow(), client.getTimeWindow()) &&
                Objects.equals(getCoordinate(), client.getCoordinate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDemand(), getTimeWindow(), getServiceTime(), getCoordinate());
    }

    public int getId() {
        return id;
    }

    public float getDemand() {
        return demand;
    }

    public void setDemand(float demand) {
        this.demand = demand;
    }

    public TimeWindow getTimeWindow() {
        return timeWindow;
    }

    public float getServiceTime() {
        return serviceTime;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }
}
