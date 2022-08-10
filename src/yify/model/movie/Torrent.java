package yify.model.movie;

public class Torrent {
	private String url;
	private String quality;
	private String type;
	private int seeds;
	private int peers;
	private String size;

	public Torrent(String url, String quality, String type, int seeds, int peers, String size) {
		setUrl(url);
		setQuality(quality);
		setType(type);
		setSeeds(seeds);
		setPeers(peers);
		setSize(size);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		if ("".equals(url) || url == null) {
			throw new IllegalArgumentException();
		}
		this.url = url;
	}

	public String getQuality() {
		return quality;
	}

	public void setQuality(String quality) {
		if ("".equals(quality) || quality == null) {
			throw new IllegalArgumentException();
		}

		this.quality = quality;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		if ("".equals(type) || type == null) {
			throw new IllegalArgumentException();
		}

		if ("bluray".equals(type)) {
			type = "BluRay";
		} else if ("web".equals(type)) {
			type = "WEB";
		}

		this.type = type;
	}

	public int getSeeds() {
		return seeds;
	}

	public void setSeeds(int seeds) {
		this.seeds = seeds;
	}

	public int getPeers() {
		return peers;
	}

	public void setPeers(int peers) {
		this.peers = peers;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

}
