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


import java.util.ArrayList;
import java.util.Collections;

public class Chromosome {
    private ArrayList<Client> sequence;
    private Problem problem;

    public Chromosome(Problem problem) {
        this.problem = problem;
        sequence = new ArrayList<>();
        problem.getClients().forEach(client -> sequence.add(client));
        sequence.remove(0);
        Collections.shuffle(sequence);
    }

    public ArrayList<Client> getSequence() {
        return sequence;
    }

    public Problem getProblem() {
        return problem;
    }

    public double decode() {
        double cost;
        ArrayList<Double> v = new ArrayList<>();

        for (int i = 0; i < sequence.size(); i++) {
            v.add(Double.POSITIVE_INFINITY);

        }
        for (int i = 0; i < sequence.size(); i++) {
            double time = 0, load = 0, distance = 0;
            int j = i;
            boolean stop = false;
            do {
                load += sequence.get(j).getDemand();
                if (i == j) {
                    time = Math.max(problem.getDistance(problem.getDepot(), sequence.get(j)),
                            sequence.get(j).getTimeWindow().getEarliestTime());
                    distance = problem.getDistance(problem.getDepot(), sequence.get(j));
                } else {
                    time = Math.max(time - problem.getDistance(sequence.get(j - 1), problem.getDepot())
                                    + problem.getDistance(sequence.get(j - 1), sequence.get(j)),
                            sequence.get(j).getTimeWindow().getEarliestTime());
                    distance = distance - problem.getDistance(sequence.get(j), problem.getDepot())
                            + problem.getDistance(sequence.get(j - 1), sequence.get(j));
                }

                if (load > problem.getVehicle().getCapacity() || time > sequence.get(j).getTimeWindow().getLatestTime()) {
                    stop = true;
                } else {
                    time = time + sequence.get(j).getServiceTime() + problem.getDistance(sequence.get(j), problem.getDepot());
                    distance = distance + problem.getDistance(sequence.get(j), problem.getDepot());
                    double lastCost = (i == 0) ? 0.0 : v.get(i - 1);
                    if (lastCost + distance < v.get(j)) {
                        v.set(j, lastCost + distance);
                    }
                    j++;
                }
            } while (!stop && j < sequence.size());
        }
        cost = v.get(sequence.size() - 1);
        return cost;
    }
}
