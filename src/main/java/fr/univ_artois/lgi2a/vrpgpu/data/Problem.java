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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.IntStream;


public class Problem {

    protected String name;
    protected String filepath;
    protected ArrayList<Client> clients;
    protected Vehicle vehicle;
    protected Depot depot;
    protected ArrayList<ArrayList<Double>> distances;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public int getNbClients() {
        return this.clients.size();
    }

    public int getNbVehicles() {
        return this.clients.size() / 4;
    }

    public Client getClient(int id) {
        return this.clients.get(id);
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    public Double getDistance(Client departure, Client arrival) {
        return this.distances.get(departure.getId()).get(arrival.getId());
    }

    public Double getDistance(Depot depot, Client arrival) {
        return this.distances.get(0).get(arrival.getId());
    }

    public Double getDistance(Client departure, Depot depot) {
        return this.distances.get(departure.getId()).get(0);
    }

    public float[] getFlatDistanceMatrix() {
        float[] list = new float[distances.size() * distances.size()];
        for (int i = 0; i < distances.size(); i++) {
            for (int j = 0; j < distances.get(i).size(); j++) {
                list[distances.size() * i + j] = distances.get(i).get(j).floatValue();
            }
        }
        return list;
    }

    public float[] getFlatTwOpen() {
        float[] twOpen = new float[clients.size()];
        IntStream.range(0, twOpen.length).forEach(i -> twOpen[i] = (float) clients.get(i).getTimeWindow().getEarliestTime());
        return twOpen;
    }

    public float[] getFlatTwClose() {
        float[] twClose = new float[clients.size()];
        IntStream.range(0, twClose.length).forEach(i -> twClose[i] = (float) clients.get(i).getTimeWindow().getLatestTime());
        return twClose;
    }

    public float[] getFlatService() {
        float[] services = new float[clients.size()];
        IntStream.range(0, services.length).forEach(i -> services[i] = (float) clients.get(i).getServiceTime());
        return services;
    }

    public float[] getFlatDemands() {

        float[] demands = new float[clients.size()];
        IntStream.range(0, demands.length).forEach(i -> demands[i] = (float) clients.get(i).getDemand());
        return demands;
    }

    /**
     * @param path instance file
     */
    public void read(String path) {

        this.setFilepath(path);
        try {
            this.parse(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parse(String path) throws IOException {
        BufferedReader reader;

        reader = new BufferedReader(new FileReader(path));
        String line = reader.readLine();
        this.setName(line);
        this.clients = new ArrayList<Client>();
        while (line != null) {
            if (line.contains("VEHICLE")) {
                reader.readLine();
                line = reader.readLine();
                String[] fields = line.split("\\s+");
                this.setVehicle(new Vehicle(Double.parseDouble(fields[2])));
            }
            if (line.contains("CUSTOMER")) {
                reader.readLine();
                reader.readLine();
                line = reader.readLine();
                while (line != null) {
                    String[] fields = line.split("\\s+");
                    Client client = new Client(
                            Integer.parseInt(fields[1]),
                            new Coordinate(
                                    Double.parseDouble(fields[2]),
                                    Double.parseDouble(fields[3])
                            ),
                            Double.parseDouble(fields[4]),
                            new TimeWindow(
                                    Double.parseDouble(fields[5]),
                                    Double.parseDouble(fields[6])
                            ),
                            Double.parseDouble(fields[7])
                    );
                    this.getClients().add(client);
                    line = reader.readLine();
                }
                if (this.getNbClients() > 0) {
                    this.setDepot(new Depot(
                            this.getClient(0).getCoordinate(),
                            this.getClient(0).getTimeWindow().getLatestTime()));

                    this.distances = new ArrayList<>(this.clients.size());
                    this.clients.forEach(departure -> {
                        this.distances.add(new ArrayList<>(this.clients.size()));
                        this.clients.forEach(arrival ->
                                this.distances.get(departure.getId()).add(
                                        Math.sqrt(
                                                Math.pow(departure.getCoordinate().getX() - arrival.getCoordinate().getX(), 2.0) +
                                                        Math.pow(departure.getCoordinate().getY() - arrival.getCoordinate().getY(), 2.0)
                                        )
                                )
                        );
                    });

                }
            }
            line = reader.readLine();
        }
        reader.close();
    }
}
