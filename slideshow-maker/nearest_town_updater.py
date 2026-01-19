from math import radians, sin, cos, sqrt, atan2
import sys
import os
import sqlite3

tamilnadu_towns = {
    'adhirampattinam':(10.339798373566042, 79.38137298925807),
    'ambasamudram': (8.7102, 77.4552),
    'ambur': (12.7454, 78.7241),
    'arani': (12.6667, 79.2667),
    'aranthangi':(10.173668449629103, 78.9978555777781),
    'ariyalur': (11.0941, 79.0885),
    'arakkonam': (13.0845, 79.6700),
    'aruppukottai': (9.4111, 77.2947),
    'aaththoor': (11.5909, 78.6020),
    'avinashi': (11.1887, 77.2696),
    'bhavani': (11.4471, 77.6841),
    'bodinayakkanur': (10.0104, 77.3526),
    'chatrapathi shambaji nagar':(19.875430825396045, 75.3309936988399),
    'chengalpattu': (12.7187, 79.9801),
    'chengam': (12.3087, 78.7925),
    # 'chennai': (13.0827, 80.2707),
    'chidambaram': (11.3995, 79.6935),
    'chinnasalem': (11.7562, 79.1950),
    'coimbatore': (11.0168, 76.9558),
    'cuddalore': (11.7480, 79.7714),
    'cumbum': (9.7366, 77.2801),
    'devakottai': (9.943380997109553, 78.8193138198166),
    'dharapuram': (10.735171771240232, 77.53393806006522),
    'dharmapuri': (12.1073, 78.1602),
    'dhindukkal': (10.361239218724524, 77.97057363327852),
    'edappadi': (11.5810, 77.8474),
    'ernakulam':(9.985742191211422, 76.31927548100089),
    'erode': (11.3410, 77.7172),
    'ettayapuram': (9.1430, 77.9880),
    'senji': (12.2526, 79.4193),
    'gobichettipalayam': (11.4540, 77.4426),
    'gudiyatham': (12.9460, 78.8674),
    'harur': (11.7945, 78.6496),
    'hosur': (12.7337, 77.8238),
    'jeyankondam':(11.21328017020533, 79.3622491856815),
    'kangeyam': (11.003109810195728, 77.56159170971536),
    'karaikkal': (10.9265, 79.8348),
    'karaikudi': (10.0705, 78.7672),
    'karur': (10.9601, 78.0807),
    # 'katpadi': (12.9891, 79.1336),
    'kallakurichi': (11.737029529560093, 78.96154806458905),
    'kanchipuram': (12.8333, 79.7072),
    'kamuthi': (9.4083, 78.3792),
    'kanyakumari': (8.0784, 77.5619),
    'karaikkal': (10.9265, 79.8348),
    'kattumannaarkoil': (11.276617105144359, 79.55155119022584),
    'kollam':(8.918391544913701, 76.61517250289394),
    'kottayam':(9.59563892479195, 76.52539817014662),
    'kovilpatti': (9.1715, 77.8707),
    'krishnagiri': (12.8310, 78.2070),
    'kudavasal': (10.857557664661321, 79.48200790214531),
    'kumbakonam': (10.958767445843389, 79.3775739819758),
    'kuzhithurai': (8.3206, 77.2101),
    'lalgudi': (10.8742, 78.8191),
    'madurai': (9.91923669251282, 78.12106543450419),
    'mamallapuram': (12.6222, 80.1939),
    'manamadurai': (9.6734, 78.4709),
    'manamelkudi':(10.039706930708002, 79.23036557983798),
    'mannargudi': (10.6670, 79.4507),
    'manapparai': (10.6080, 78.4234),
    'mangalore': (12.9029, 74.8521),
    'marakkanam': (12.1949, 79.9334),
    # 'marthandam': (8.3099, 77.2189),
    'mayavaram': (11.1035, 79.6526),
    'mettupalayam': (11.3000, 76.9500),
    'melur': (10.0333, 78.3167),
    'mettur anai': (11.7894, 77.8008),
    'muthuppettai': (10.3995, 79.4885),
    'nagapattinam': (10.7649, 79.8415),
    'nagercoil': (8.1782, 77.4323),
    'namakkal': (11.2220, 78.1662),
    'nanded': (19.153033015123345, 77.31962419072391),
    'nanguneri': (8.7081, 77.7315),
    'nannilam': (10.8795, 79.6139),
    'neyveli': (11.5333, 79.4800),
    'nidamangalam': (10.772156326866092, 79.41620576146238),
    # 'orathanadu': (10.5714, 79.2607),
    'parani': (10.4602, 77.5256),
    # 'palayamkottai': (8.7300, 77.7112),
    'palladam': (10.995240808830818, 77.28408521960056),
    'paalakkaadu': (10.776528859370202, 76.65613785360475),
    'palakkodu': (12.29054154801255, 78.06893877124809),
    'panruti': (11.7766, 79.5582),
    'paramakkudi': (9.5464, 78.5904),
    # 'peralam':(10.961967400264063, 79.65979779041157),
    'perambalur': (11.1633, 78.7334),
    'peravurani': (10.2937, 79.2029),
    'perundurai': (11.2755, 77.5871),
    'pollachi': (10.6582, 77.0085),
    'pondicherry': (11.9378, 79.8134),
    'poompuhar': (11.1453, 79.8566),
    'pattukkottai': (10.4236, 79.3192),
    'pudhukkottai': (10.378263071139637, 78.81469342501299),
    'rajapalayam': (9.446549563686096, 77.55359805176153),
    'rameswaram': (9.2886, 79.3129),
    'ramanathapuram': (9.3718, 78.8302),
    'ranipet': (12.9425, 79.3304),
    'salem': (11.65563283570127, 78.14926160940786),
    'sankarankovil': (9.1700, 77.5400),
    'sankarankoil': (9.1431, 77.4768),
    'sathyamangalam': (11.5059, 77.2383),
    'satur': (9.5466, 77.4272),
    # 'sathankulam': (8.4445, 77.9176),
    'sirkazhi': (11.2371, 79.7356),
    'sivakasi': (9.450877654050071, 77.79601221342476),
    # 'srirangam': (10.8567914328034, 78.69664013461447),
    'sriperumbudur': (12.966552047483939, 79.94682628977866),
    # 'suchindram': (8.1580, 77.4702),
    # 'sulur': (11.0331, 77.1252),
    'thindivanam': (12.2340, 79.6566),
    'thanjavur': (10.77614206199652, 79.14038293516401),
    'thalaignayiru': (10.562429822162779, 79.77265164263247),
    'theni': (10.010644447478539, 77.48100865874032),
    'thirumangalam': (9.8333, 77.9833),
    'thiruppathur':(12.490503948583301, 78.56771735739989),
    'thiruvaiyaru': (10.879795617191254, 79.10382308305115),
    'thiruvarur': (10.7667, 79.6365),
    'thiruvallur': (13.141012346747383, 79.90726539487835),
    'thiruthani': (13.1779, 79.6090),
    'thiruchengode': (11.3786, 77.8949),
    'thiruthuraippoondi': (10.5358, 79.6376),
    'thondi': (9.7415, 79.0173),
    'thiruchchirappalli': (10.822621546130751, 78.68946504908261),
    'thiruchendur':(8.49615383290306, 78.12194062331153),
    'thirunelveli': (8.727752364418652, 77.70432906905977),
    'thiruttani': (13.4211, 79.5702),
    'thiruvannamalai': (12.2253, 79.0747),
    'thiruppur': (11.1095, 77.3413),
    'thenkasi': (8.9583, 77.3156),
    'thoothukudi': (8.804780480071441, 78.14763422319969),
    'thirukkattupalli': (10.850412229672013, 78.95445411510904),
    'udumalaippettai': (10.583865554459399, 77.24824053502849),
    'usilampatti': (9.9637, 77.7912),
    'uthamapalayam': (9.8063, 77.3273),
    'uthiramerur': (12.6000, 79.7500),
    'vadalur': (11.5333, 79.4800),
    'valliyur': (8.3769, 77.6108),
    'valparai': (10.3360, 76.9515),
    'vandavasi': (12.5043, 79.6168),
    'vaniyambadi': (12.6813, 78.6200),
    'vasudevanallur': (9.2296, 77.4101),
    'vedharanyam': (10.375260560659962, 79.84907839631343),
    'vaeloor': (12.9165, 79.1325),
    'virudhachalam': (11.5146, 79.3212),
    'virudhunagar': (9.5874, 77.9571),
    'viruppuram': (11.9363, 79.4909),
    'walajabad': (12.8305, 79.7370),
    'port_blair': (11.6234, 92.7265),  # Andaman and Nicobar Islands
    'amaravati': (16.5735, 80.3575),  # Andhra Pradesh
    'itanagar': (27.0844, 93.6053),   # Arunachal Pradesh
    'dispur': (26.1445, 91.7362),     # Assam
    'patna': (25.5941, 85.1376),      # Bihar
    'raipur': (21.2514, 81.6296),     # Chhattisgarh
    'panaji': (15.4909, 73.8278),     # Goa
    'gandhinagar': (23.2156, 72.6369),# Gujarat
    'chandigarh': (30.7333, 76.7794), # Haryana and Punjab
    'shimla': (31.1048, 77.1734),     # Himachal Pradesh
    'srinagar': (34.0837, 74.7973),   # Jammu (Winter Capital, J&K)
    'jammu': (32.7266, 74.8570),      # Jammu (Summer Capital, J&K)
    'ranchi': (23.3441, 85.3096),     # Jharkhand
    'bengaluru': (12.9716, 77.5946),  # Karnataka
    'thiruvananthapuram': (8.5241, 76.9366), # Kerala
    'bhopal': (23.2599, 77.4126),     # Madhya Pradesh
    'mumbai': (19.0760, 72.8777),     # Maharashtra
    'imphal': (24.8170, 93.9368),     # Manipur
    'shillong': (25.5788, 91.8933),   # Meghalaya
    'aizawl': (23.7271, 92.7176),     # Mizoram
    'kohima': (25.6751, 94.1086),     # Nagaland
    'bhubaneswar': (20.2961, 85.8245),# Odisha
    'jaipur': (26.9124, 75.7873),     # Rajasthan
    'gangtok': (27.3389, 88.6065),    # Sikkim
    'madras': (13.0827, 80.2707),    # Tamil Nadu
    'hyderabad': (17.3850, 78.4867),  # Telangana
    'agartala': (23.8315, 91.2868),   # Tripura
    'lucknow': (26.8467, 80.9462),    # Uttar Pradesh
    'dehradun': (30.3165, 78.0322),   # Uttarakhand
    'kolkata': (22.5726, 88.3639),    # West Bengal
    'delhi': (28.6139, 77.2090),      # Delhi (National Capital Territory)
    'thirupathi': (13.618629962284494, 79.41722934697964),      # Delhi (National Capital Territory)
    'leh': (34.1526, 77.5770)        # Ladakh
}


# Function to calculate the distance between two lat-long coordinates using the Haversine formula
def haversine_distance(lat1, lon1, lat2, lon2):
    # Convert latitude and longitude from degrees to radians
    lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])

    # Haversine formula
    dlat = lat2 - lat1
    dlon = lon2 - lon1
    a = sin(dlat / 2)**2 + cos(lat1) * cos(lat2) * sin(dlon / 2)**2
    c = 2 * atan2(sqrt(a), sqrt(1 - a))

    # Radius of Earth in kilometers (mean radius)
    radius = 6371.0
    return radius * c

# Dictionary of Tamil Nadu towns and their lat-long coordinates
# Extended dictionary of towns with latitude and longitude in Tamil Nadu
# The dictionary now includes a variety of second-tier and third-tier towns across Tamil Nadu

# Function to find the closest town
def find_closest_town(lat, lon):
    closest_town = None
    min_distance = float('inf')
    
    for town, (town_lat, town_lon) in tamilnadu_towns.items():
        distance = haversine_distance(lat, lon, town_lat, town_lon)
        if distance < min_distance:
            min_distance = round(distance,2)
            closest_town = town
    
    return closest_town, min_distance

def update_temples_with_closest_town(db_path):
    # Connect to the SQLite database
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    # Fetch rows where dm is not null
    cursor.execute("SELECT dm, latlong FROM temples WHERE dm IS NOT NULL and nearby_town is null  OR TRIM(nearby_town) = ''")
    rows = cursor.fetchall()

    # Process each row
    for row in rows:
        dm, latlong = row
        if latlong:
            # Split latlong into latitude and longitude
            lat, lon = map(float, latlong.split(','))

            # Call your function to get the closest town and distance
            town_name, distance = find_closest_town(lat, lon)

            # Update the temples table with the nearby town and distance
            cursor.execute(
                "UPDATE temples SET nearby_town = ?, distance = ? WHERE dm = ?",
                (town_name, distance, dm)
            )
            print(f"Updated dm={dm}: nearby_town={town_name}, distance={distance}")

    # Commit changes and close the connection
    conn.commit()
    conn.close()


# Main function to handle command line arguments
def main():
    # Example usage
    db_path = "/Users/sriram/Desktop/yard/rest/bin/database/temples.db"  # Replace with your SQLite database path
    update_temples_with_closest_town(db_path)


if __name__ == "__main__":
    main()

