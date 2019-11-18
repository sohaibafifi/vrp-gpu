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

package fr.univ_artois.lgi2a.vrpgpu;


import com.aparapi.Kernel;
import fr.univ_artois.lgi2a.vrpgpu.data.Chromosome;
import fr.univ_artois.lgi2a.vrpgpu.data.Problem;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Decoder extends Kernel {
    final int n;
    final float[] twOpen, twClose, demand, service, distances;
    final float capacity;
    protected int[] gsequence;
    protected @PrivateMemorySpace(100)
    short[] sequence = new short[100];
    protected @PrivateMemorySpace(101)
    float[] v = new float[101];
    float[] costs;

    public Decoder(Chromosome chromosome) {

        Problem problem = chromosome.getProblem();
        this.costs = new float[problem.getNbClients() * problem.getNbClients() ];
        IntStream.range(0, this.costs.length).forEach(i -> this.costs[i] = Float.POSITIVE_INFINITY);
        this.distances = problem.getFlatDistanceMatrix();
        this.twOpen = problem.getFlatTwOpen();
        this.twClose = problem.getFlatTwClose();
        this.demand = problem.getFlatDemands();
        this.service = problem.getFlatService();
        this.capacity = (float) problem.getVehicle().getCapacity();
        this.gsequence = new int[chromosome.getSequence().size()];
        Arrays.setAll(this.gsequence, i -> chromosome.getSequence().get(i).getId());
        this.n = problem.getNbClients();

        setExplicit(true);
        put(distances);
        put(twClose);
        put(twOpen);
        put(demand);
        put(service);
        put(gsequence);
        put(costs);

    }

    public float[] getCosts() {
        get(costs);
        return costs;
    }

    @Override
    public void run() {
        copySequence();
        int x = getGlobalId(0) % n;
        int y = getGlobalId(1) % n;


        float cost = evaluate(x, y);

        costs[x * n + y] = cost;

    }

    public void copySequence() {
        for (int i = 0; i < v.length; i++) {
            v[i] = Float.POSITIVE_INFINITY;
        }
        for (int i = 0; i < n - 1; i++) {
            sequence[i] = (short) gsequence[i];
        }
    }

    public float evaluate(int x, int y) {
        short t = sequence[x];
        sequence[x] = sequence[y];
        sequence[y] = t;
        float cost;
        for (int i = 0; i < n; i++) {
            float time = 0, load = 0, distance = 0;
            for (int j = i; j < n
                    && load < capacity
                    && time < twClose[sequence[j]]; ) {
                load += demand[sequence[j]];
                if (load < capacity) {
                    if (i == j) {
                        time = max(distances[sequence[j]], twOpen[sequence[j]]);
                        distance = distances[sequence[j]];
                    } else {
                        time = max(time - distances[sequence[j - 1] * (n)] + distances[sequence[j - 1] * (n) + sequence[j]],
                                twOpen[sequence[j]]);
                        distance = distance - distances[sequence[j - 1] * (n)] + distances[sequence[j - 1] * (n) + sequence[j]];
                    }
                    if (time < twClose[sequence[j]]) {
                        time = time + service[sequence[j]] + distances[sequence[j] * (n)];
                        distance = distance + distances[sequence[j] * (n)];
                        float lastCost = (i == 0) ? 0 : v[i - 1];
                        if (lastCost + distance < v[j]) {
                            v[j] = lastCost + distance;
                        }
                        j++;
                    }
                }
            }
        }
        cost = v[n - 1];
        return cost;
    }
}
