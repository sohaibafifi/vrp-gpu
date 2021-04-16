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


import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.internal.kernel.KernelManager;
import com.aparapi.internal.kernel.KernelPreferences;
import fr.univ_artois.lgi2a.vrpgpu.solvers.Decoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class Chromosome {
    private final List<Client> sequence;
    private final Problem problem;

    public Chromosome(Problem problem) {
        this.problem = problem;
        sequence = new ArrayList<>();
        sequence.addAll(problem.getClients());
        sequence.remove(0);
        Collections.shuffle(sequence);
    }

    public Chromosome(Problem problem, List<Client> sequence) {
        this.problem = problem;
        this.sequence = sequence;
    }



    public List<Client> getSequence() {
        return sequence;
    }

    public Problem getProblem() {
        return problem;
    }

    public void swap(int x, int y) {
        Client t = sequence.get(x);
        sequence.set(x, sequence.get(y));
        sequence.set(y, t);
    }

    public Float decode() {
        Float cost;
        ArrayList<Float> v = new ArrayList<>();

        for (int i = 0; i < sequence.size(); i++) {
            v.add(Float.POSITIVE_INFINITY);

        }
        for (int i = 0; i < sequence.size(); i++) {
            float time = 0, load = 0, distance = 0;
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
                    distance = distance - problem.getDistance(sequence.get(j - 1), problem.getDepot())
                            + problem.getDistance(sequence.get(j - 1), sequence.get(j));
                }

                if (load > problem.getVehicle().getCapacity() || time > sequence.get(j).getTimeWindow().getLatestTime()) {
                    stop = true;
                } else {
                    time = time + sequence.get(j).getServiceTime() + problem.getDistance(sequence.get(j), problem.getDepot());
                    distance = distance + problem.getDistance(sequence.get(j), problem.getDepot());
                    float lastCost = (i == 0) ? 0.0f : v.get(i - 1);
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

    public void print() {
        System.out.print("[");
        for (int i = 0; i < sequence.size(); i++) {
            System.out.print(" " + sequence.get(i).getId());
        }
        System.out.print("]  " + decode());

    }

    /**
     * The shake operator tries all the swaps
     * between every (x, y) position
     * according to the used
     * gpu processor
     */
    public void shake() {
        Decoder decoder = new Decoder(this);
        KernelPreferences preferences = KernelManager.instance().getPreferences(decoder);
        List<Device> devices = preferences.getPreferredDevices(decoder);
        if (devices.isEmpty()) return;
        int n = this.getProblem().getNbClients() - 1;
        float[] costs = new float[problem.getNbClients() * problem.getNbClients()];
        // Be sure we are using a GPU here
        Device chosen = null;
        for (Device device : devices) {
            if (device.getType() == Device.TYPE.GPU) {
                chosen = device;
                break;
            }
        }
        decoder.execute(Range.create2D(chosen, n, n)).get(costs);

        float[] result = decoder.getCosts();
        float minimum = result[0];
        int x = 0, y = 0;
        for (int i = 0; i < result.length; i++) {
            if (result[i] < minimum) {
                minimum = result[i];
                x = i / problem.getNbClients();
                y = i % problem.getNbClients();
            }
        }

        decoder.dispose();
        this.swap(x, y);
    }

    public Chromosome copy() {
        List<Client> sequence = new ArrayList<>();
        IntStream.range(0, this.sequence.size()).forEach(i -> sequence.add(this.sequence.get(i)));
        return new Chromosome(this.problem, sequence);
    }
}
