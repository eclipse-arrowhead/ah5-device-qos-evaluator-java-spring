import psutil
import time

def network_load_percent(interval=1):
    # Get initial counters
    net1 = psutil.net_io_counters(pernic=True)
    time.sleep(interval)
    net2 = psutil.net_io_counters(pernic=True)
    
    # Get NIC stats (including speed in Mbps)
    stats = psutil.net_if_stats()
    
    # Get NIC addresses
    addrs = psutil.net_if_addrs()
    
    results = {}
    for iface in net1:
        if iface not in stats or stats[iface].speed == 0:
            continue  # skip interfaces with unknown speed

        # Calculate ingress (received) and egress (sent) in bits/sec
        bytes_in = net2[iface].bytes_recv - net1[iface].bytes_recv
        bytes_out = net2[iface].bytes_sent - net1[iface].bytes_sent
        bps_in = (bytes_in * 8) / interval
        bps_out = (bytes_out * 8) / interval

        link_speed_bps = stats[iface].speed * 1_000_000  # Mbps → bps
        ingress_percent = (bps_in / link_speed_bps) * 100
        egress_percent = (bps_out / link_speed_bps) * 100

        # Gather assigned IP addresses
        iface_addrs = []
        if iface in addrs:
            for addr in addrs[iface]:
                if addr.family.name == "AF_INET":
                    iface_addrs.append(f"IPv4: {addr.address}")
                elif addr.family.name == "AF_INET6":
                    iface_addrs.append(f"IPv6: {addr.address}")
                elif addr.family.name == "AF_LINK":
                    iface_addrs.append(f"MAC: {addr.address}")

        print(f"{iface}:")
        print(f"  Assigned addresses: {', '.join(iface_addrs)}")
        print(f"  Link speed: {link_speed_bps} bps")
        print(f"  Ingress: {bps_in:.0f} bps ({ingress_percent:.6f}%)")
        print(f"  Egress: {bps_out:.0f} bps ({egress_percent:.6f}%)\n")
    
    return results

network_load_percent()
