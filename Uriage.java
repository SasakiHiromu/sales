package jp.alhinc.sasaki_hiromu.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Uriage {
	public static void main(String[] args) {
		HashMap<String, String> branchShop = new HashMap<String, String>() ;
		HashMap<String, Long> branchSum = new HashMap<String, Long>() ;
		BufferedReader br = null;

		try {
			if (!(args.length == 1)) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
			File file = new File(args[0], "branch.lst");
			if (!file.exists()) {
				System.out.println("支店定義ファイルが存在しません");
				return;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null) {
				String[] shops = s.split(",", -1);
				if (shops[0].matches("^\\d{3}") && shops.length == 2) {
					branchShop.put(shops[0], shops[1]);
					branchSum.put(shops[0], 0L);
				} else {
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
			}
		} catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

		HashMap<String, String> commodityBy = new HashMap<String, String>() ;
		HashMap<String, Long> commoditySum = new HashMap<String, Long>() ;

		try {
			File file = new File(args[0], "commodity.lst");
			if (!file.exists()) {
				System.out.println("商品定義ファイルが存在しません");
				return;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null) {
				String[] masins = s.split(",", -1);

				if (masins[0].matches("^\\w{8}") && masins.length == 2) {
					commodityBy.put(masins[0], masins[1]);
					commoditySum.put(masins[0], 0L);
				} else {
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
			}
		} catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

		File dir = new File(args[0]);
		ArrayList<File> file = new ArrayList<File>();
		File[] files = dir.listFiles();

		for (int i = 0; i < files.length; i++) {
			String filename = files[i].getName();
			if (filename.matches("^\\d{8}.rcd") && files[i].isFile()) {
				file.add(files[i]);
			}
		}

		for (int i = 0; i < file.size(); i++) {
			String filename = file.get(i).getName();
			String name = filename.substring(1,8);
			int number = Integer.parseInt(name);
			if (!(number == i + 1)) {
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}
		ArrayList<Map.Entry<String,Long>> branchDown =
				new ArrayList<Map.Entry<String,Long>>(branchSum.entrySet());
		ArrayList<Map.Entry<String,Long>> commodityDown =
				new ArrayList<Map.Entry<String,Long>>(commoditySum.entrySet());

		try {
			for (int i = 0; i < file.size(); i++) {
				br = new BufferedReader(new FileReader(file.get(i)));
				ArrayList<String> payList = new ArrayList<String>();

				try {
					String line;
					while ((line  = br.readLine()) != null) {
						payList.add(line);
					}
					if (!(payList.size() == 3)) {
						System.out.println(file.get(i).getName() + "のフォーマットが不正です");
						return;
					}
					if (!(payList.get(2).matches("\\d[0-9]+$"))) {
						System.out.println("予期せぬエラーが発生しました");
						return;
					}
					if (branchSum.containsKey(payList.get(0))) {
						Long cord = branchSum.get(payList.get(0));
						cord += Long.parseLong(payList.get(2));
						branchSum.put(payList.get(0), cord);
					} else {
						System.out.println(file.get(i).getName() + "の支店コードが不正です");
						return;
					}
					if (commoditySum.containsKey(payList.get(1))) {
						Long sale = commoditySum.get(payList.get(1));
						sale += Long.parseLong(payList.get(2));
						commoditySum.put(payList.get(1), sale);
					} else {
						System.out.println(file.get(i).getName() + "の商品コードが不正です");
						return;
					}
					Long branchLast = branchSum.get(payList.get(0));
					Long commodityLast = commoditySum.get(payList.get(1));
					if (!(branchLast <= 9999999999L) || !(commodityLast <= 9999999999L)) {
						System.out.println(file.get(i).getName() + "の合計金額が10桁を超えました");
						return;
					}
				}
				catch(FileNotFoundException e) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				} finally {
					try {
						if (br != null) {
							br.close();
						}
					}catch (IOException e) {
						System.out.println("予期せぬエラーが発生しました");
						return;
					}
				}
			}
			Collections.sort(branchDown, new Comparator<Map.Entry<String,Long>>() {
				public int compare(
						Entry<String,Long> branchDown1, Entry<String,Long> branchDown2) {
					return ((Long)branchDown2.getValue()).compareTo((Long)branchDown1.getValue());
				}
			});
			Collections.sort(commodityDown, new Comparator<Map.Entry<String,Long>>() {
				public int compare(
						Entry<String,Long> commodityDown1, Entry<String,Long> commodityDown2) {
					return ((Long)commodityDown2.getValue()).compareTo((Long)commodityDown1.getValue());
				}
			});
		}
		catch( IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;

		}
		File branchOut = new File(args[0], "branch.out");
		File commodityOut = new File(args[0], "commodity.out");
		BufferedWriter bw = null;

		try {
			branchOut.createNewFile();
			FileWriter fw = new FileWriter(branchOut);
			bw = new BufferedWriter(fw);
			for (Entry<String,Long> branchSort : branchDown) {
				bw.write(branchSort.getKey() + ","  +
						branchShop.get(branchSort.getKey()) + "," +  branchSort.getValue());
				bw.newLine();
			}
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			}catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

		try {
			commodityOut.createNewFile();
			FileWriter fw = new FileWriter(commodityOut);
			bw = new BufferedWriter(fw);
			for (Entry<String,Long> commoditySort : commodityDown) {
				bw.write(commoditySort.getKey() + ","  +
						commodityBy.get(commoditySort.getKey()) + "," +  commoditySort.getValue());
				bw.newLine();
			}
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			}catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}
	}
}