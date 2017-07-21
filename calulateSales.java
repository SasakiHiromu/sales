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

public class calulateSales {
	public static void main(String[] args) {
		HashMap<String, String> branchShop = new HashMap<String, String>() ;
		HashMap<String, Long> branchSum = new HashMap<String, Long>() ;
		HashMap<String, String> commodityBy = new HashMap<String, String>() ;
		HashMap<String, Long> commoditySum = new HashMap<String, Long>() ;
		if (!(args.length == 1)) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		BufferedReader br = null;

		if (fileRead(args[0],"branch.lst", "支店", "^\\d{3}", branchShop, branchSum)) {
		} else {
			return;
		}
		if (fileRead(args[0],"commodity.lst", "商品", "^\\w{8}", commodityBy, commoditySum)) {
		} else {
			return;
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
					if (!(payList.get(2).matches("\\d+$"))) {
						System.out.println("予期せぬエラーが発生しました");
						return;
					}
					if (!(branchSum.containsKey(payList.get(0)))) {
						System.out.println(file.get(i).getName() + "の支店コードが不正です");
						return;
					}
					if (!(commoditySum.containsKey(payList.get(1)))) {
						System.out.println(file.get(i).getName() + "の商品コードが不正です");
						return;
					}
					//Long branchLast = branchSum.get(payList.get(0));
					//Long commodityLast = commoditySum.get(payList.get(1));

					Long cord = branchSum.get(payList.get(0));
					cord += Long.parseLong(payList.get(2));

					Long sale = commoditySum.get(payList.get(1));
					sale += Long.parseLong(payList.get(2));
					if (cord > 9999999999L) {
						System.out.println("合計金額が10桁を超えました");
						return;
					}
					if (sale > 9999999999L) {
						System.out.println("合計金額が10桁を超えました");
						return;
					}
					branchSum.put(payList.get(0), cord);
					commoditySum.put(payList.get(1), sale);
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
		}
		catch( IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		if (fileOut(args[0], "branch.out", branchSum, branchShop)) {
		} else {
			return;
		}
		if (fileOut(args[0], "commodity.out", commoditySum, commodityBy)) {
		} else {
			return;
		}
	}

	public static boolean fileOut (String dir, String fileName,
			HashMap<String,Long> sales, HashMap<String,String> names) {
		ArrayList<Map.Entry<String,Long>> branchDown =
				new ArrayList<Map.Entry<String,Long>>(sales.entrySet());
		Collections.sort(branchDown, new Comparator<Map.Entry<String,Long>>() {
			public int compare(
					Entry<String,Long> branchDown1, Entry<String,Long> branchDown2) {
				return ((Long)branchDown2.getValue()).compareTo((Long)branchDown1.getValue());
			}
		});
		File branchOut = new File(dir, fileName);
		BufferedWriter bw = null;

		try {
			branchOut.createNewFile();
			FileWriter fw = new FileWriter(branchOut);
			bw = new BufferedWriter(fw);
			for (Entry<String,Long> branchSort : branchDown) {
				bw.write(branchSort.getKey() + ","  +
						names.get(branchSort.getKey()) + "," +  branchSort.getValue());
				bw.newLine();
			}
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		} finally {
			try {
				if (bw != null) {
					bw.close();
				}
			}catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;
	}

	public static boolean fileRead (String dir, String fileName, String branchCommodity, String base,
			HashMap<String,String> names, HashMap<String,Long> sales) {
		BufferedReader br = null;

		try {
			File file = new File(dir, fileName);
			if (!file.exists()) {
				System.out.println(branchCommodity + "定義ファイルが存在しません");
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null) {
				String[] shops = s.split(",", -1);
				if (shops[0].matches(base) && shops.length == 2) {
					names.put(shops[0], shops[1]);
					sales.put(shops[0], 0L);
				} else {
					System.out.println(branchCommodity + "定義ファイルのフォーマットが不正です");
					return false;
				}
			}
		} catch(IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;
	}
}