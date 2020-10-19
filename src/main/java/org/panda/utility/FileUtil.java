package org.panda.utility;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author Ozgun Babur
 */
public class FileUtil
{
	public static ZipEntry findEntryContainingNameInZIPFile(String zipFileName,
		String partOfEntryName)
	{
		return findEntryContainingNameInZIPFile(zipFileName, partOfEntryName, null);
	}

	public static ZipEntry findEntryContainingNameInZIPFile(String zipFileName,
		String partOfEntryName, String tabooPartOfEntryName)
	{
		try
		{
			ZipFile zipFile = new ZipFile(zipFileName);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			while (entries.hasMoreElements())
			{
				ZipEntry zipEntry = entries.nextElement();
				if (!zipEntry.isDirectory())
				{
					String fileName = zipEntry.getName();
					if (fileName.contains(partOfEntryName) &&
						(tabooPartOfEntryName == null || !fileName.contains(tabooPartOfEntryName)))
					{
						return zipEntry;
					}
				}
			}
			zipFile.close();
		} catch (IOException ioe)
		{
			ioe.printStackTrace();
			return null;
		}
		return null;
	}

	public static boolean extractEntryContainingNameInTARGZFile(String targzFileName,
		String partOfEntryName, String extractedName)
	{
		try
		{
			TarArchiveEntry entry;

			TarArchiveInputStream is = new TarArchiveInputStream(new GZIPInputStream(
				new FileInputStream(targzFileName)));

			while ((entry = is.getNextTarEntry()) != null)
			{
				if (!entry.isDirectory())
				{
					if (entry.getName().contains(partOfEntryName))
					{
						byte [] btoRead = new byte[1024];

						BufferedOutputStream bout =new BufferedOutputStream(
							new FileOutputStream(extractedName));

						int len;
						while((len = is.read(btoRead)) != -1)
						{
							bout.write(btoRead,0,len);
						}

						bout.close();
						is.close();

						return true;
					}
				}
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		return false;
	}

	public static boolean extractAllEntriesContainingNameInTARGZFile(String targzFileName,
		String partOfEntryName, String dir)
	{
		try
		{
			TarArchiveEntry entry;

			TarArchiveInputStream is = new TarArchiveInputStream(new GZIPInputStream(
				new FileInputStream(targzFileName)));

			boolean success = false;

			while ((entry = is.getNextTarEntry()) != null)
			{
				if (entry.isDirectory()) continue;
				else
				{
					String name = entry.getName();
					if (name.contains("/")) name = name.substring(name.lastIndexOf("/") + 1);
					if (name.contains(partOfEntryName))
					{
						byte [] btoRead = new byte[1024];

						BufferedOutputStream bout =new BufferedOutputStream(
							new FileOutputStream(dir + File.separator + name));

						int len;
						while((len = is.read(btoRead)) != -1)
						{
							bout.write(btoRead,0,len);
						}

						bout.close();
						success = true;
					}
				}
			}
			is.close();
			return success;
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		return false;
	}

	public static boolean extractEntryContainingNameInZipFile(String zipFileName,
		String partOfEntryName, String tabooPartOfEntryName, String extractedName)
	{
		try
		{
			ZipEntry entry = findEntryContainingNameInZIPFile(zipFileName, partOfEntryName,
				tabooPartOfEntryName);

			if (entry == null) return false;

			OutputStream out = new FileOutputStream(extractedName);
			FileInputStream fin = new FileInputStream(zipFileName);
			BufferedInputStream bin = new BufferedInputStream(fin);
			ZipInputStream zin = new ZipInputStream(bin);
			ZipEntry ze;
			while ((ze = zin.getNextEntry()) != null) {
				if (ze.getName().equals(entry.getName())) {
					byte[] buffer = new byte[8192];
					int len;
					while ((len = zin.read(buffer)) != -1) {
						out.write(buffer, 0, len);
					}
					out.close();
					break;
				}
			}
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			return false;
		}

		return true;
	}

	public static String getFileContent(String filename)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				sb.append(line).append("\n");
			}

			reader.close();
			return sb.toString();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static void printLines(String filename, String partialContent) throws IOException
	{
		printLines(filename, partialContent, Integer.MAX_VALUE);
	}

	public static void printLines(String filename, String partialContent, int limit) throws IOException
	{
		printLines(filename, new String[]{partialContent}, limit);
	}

	public static void printLines(String filename, String[] partialContent, int limit) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filename));

		int printed = 0;
		int i = 0;
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			i++;

			boolean hasAll = true;
			for (String s : partialContent)
			{
				if (!line.contains(s))
				{
					hasAll = false;
					break;
				}
			}

			if (hasAll)
			{
				System.out.println("Line " + i + ": " + line);
				if (++printed == limit) break;
			}
		}

		reader.close();
	}

	public static void printShortLines(String filename, int lengthLimit) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filename));

		int i = 0;
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			i++;

			if (line.length() <= lengthLimit)
			{
				System.out.println("Line " + i + ": " + line);
			}
		}

		reader.close();
	}

	public static void printLines(String filename, int fromLine, int toLine) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(filename));

		int i = 0;
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			i++;
			if (i >= fromLine && i <= toLine)
			{
				System.out.println(line);
			}
			if (i > toLine) break;
		}

		reader.close();
	}

	public static long countLines(String filename) { try
	{
		return Files.lines(Paths.get(filename)).count();
	}
	catch (IOException e){throw new RuntimeException(e);}}

	public static void copyFile(String src, String dest) throws IOException
	{
		File sourceFile = new File(src);
		File destFile = new File(dest);

		if(!destFile.exists())
		{
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		}
		catch (IOException e){e.printStackTrace();}
		finally {
			if(source != null) {
				source.close();
			}
			if(destination != null) {
				destination.close();
			}
		}
	}

	public static void delete(File dir)
	{
		if (dir.isDirectory())
		{
			for (File file : dir.listFiles())
			{
				delete(file);
			}
		}
		dir.delete();
	}

	public static void write(String line, Writer writer)
	{
		try
		{
			writer.write(line);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void writeln(String line, Writer writer)
	{
		write(line + "\n", writer);
	}

	public static void lnwrite(String line, Writer writer)
	{
		write("\n" + line, writer);
	}

	public static void write_tab(String token, Writer writer)
	{
		write(token + "\t", writer);
	}

	public static void tab_write(String token, Writer writer)
	{
		write("\t" + token, writer);
	}

	public static void tab_write(double val, Writer writer)
	{
		write("\t" + val, writer);
	}

	public static BufferedWriter newBufferedWriter(String file)
	{
		try
		{
			return Files.newBufferedWriter(Paths.get(file));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static void closeWriter(Writer writer)
	{
		try
		{
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void exciseFileToLines(String inFile, String outFile, LineSelector selector) { try
	{
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFile));
		Files.lines(Paths.get(inFile)).filter(selector::select).forEach(line ->
			FileUtil.writeln(line, writer));
		writer.close();
	}
	catch (IOException e){throw new RuntimeException(e);}}

	public interface LineSelector
	{
		boolean select(String line);
	}

	public static void countTermsInTabDelimitedColumn(String filename, int colIndex) { try
	{
		TermCounter tc = new TermCounter();
		Files.lines(Paths.get(filename)).map(line -> line.split("\t")).filter(token -> token.length > colIndex)
			.forEach(token -> tc.addTerm(token[colIndex]));
		tc.print();
	}
	catch (IOException e){throw new RuntimeException(e);}}

	/**
	 * Collects the encountered string in the given column of the tab delimited file into a set.
	 * @param filename
	 * @param colIndex starts from 0
	 * @return
	 */
	public static Set<String> getTermsInTabDelimitedColumn(String filename, int colIndex, int skip) { try
	{
		return Files.lines(Paths.get(filename)).skip(skip).map(line -> line.split("\t"))
			.filter(token -> token.length > colIndex).map(t -> t[colIndex]).collect(Collectors.toSet());
	}
	catch (IOException e){throw new RuntimeException(e);}}

	/**
	 * Collects the encountered string in the given column of the tab delimited file into a set.
	 * @param filename
	 * @param colIndex starts from 0
	 * @return
	 */
	public static List<String> getTermsInTabDelimitedColumnOrdered(String filename, int colIndex, int skip) { try
	{
		return Files.lines(Paths.get(filename)).skip(skip).map(line -> line.split("\t"))
			.filter(token -> token.length > colIndex).map(t -> t[colIndex]).collect(Collectors.toList());
	}
	catch (IOException e){throw new RuntimeException(e);}}

	public static Map<String, String> readMap(String filename, String delim, String keyColumn, String valueColumn) { try
	{
		String[] header = Files.lines(Paths.get(filename)).findFirst().get().split(delim);
		int keyInd = ArrayUtil.indexOf(header, keyColumn);
		int valueInd = ArrayUtil.indexOf(header, valueColumn);
		return Files.lines(Paths.get(filename)).skip(1).map(l -> l.split(delim)).collect(Collectors.toMap(t -> t[keyInd], t -> t[valueInd]));
	}
	catch (IOException e){throw new RuntimeException(e);}}

	/**
	 * Collects the rows in the file in a Set of String.
	 * @param filename
	 */
	public static Set<String> getLinesInSet(String filename) { try
	{
		return Files.lines(Paths.get(filename)).collect(Collectors.toSet());
	}
	catch (IOException e){throw new RuntimeException(e);}}

	public static void transpose(String inFile, String inDelim, String outFile, String outDelim, StringArrayProcessor p,
		boolean[] colSelect)
	{
		try
		{
			List<String[]> inList = Files.lines(Paths.get(inFile)).map(l -> l.split(inDelim)).collect(Collectors.toList());
			if (inList.isEmpty()) return;

			if (p != null)
			{
				inList.forEach(p::process);
			}

			List<String> outList = new ArrayList<>();

			int size = inList.get(0).length;

			for (int i = 0; i < size; i++)
			{
				if (colSelect != null && !colSelect[i]) continue;

				String[] s = new String[inList.size()];

				for (int j = 0; j < inList.size(); j++)
				{
					s[j] = inList.get(j)[i];
				}

				outList.add(ArrayUtil.getString(outDelim, s));
			}

			BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFile));
			outList.forEach(l -> FileUtil.writeln(l, writer));
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void fixRStyleHeader(String inFile, String delim, String outFile)
	{
		try
		{
			List<String> inList = Files.lines(Paths.get(inFile)).collect(Collectors.toList());
			if (inList.isEmpty()) return;

			BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFile));
			inList.stream().limit(1).forEach(l -> FileUtil.write(delim + l, writer));
			inList.stream().skip(1).forEach(l -> FileUtil.lnwrite(l, writer));
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void writeLinesToFile(Collection<String> lines, String filename)
	{
		try
		{
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename));
			lines.forEach(l -> writeln(l, writer));
			writer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public interface StringArrayProcessor
	{
		void process(String[] array);
	}

	public static Stream<String> lines(String filename)
	{
		try
		{
			return Files.lines(Paths.get(filename));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static Stream<String> lines(Path path)
	{
		try
		{
			return Files.lines(path);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static boolean mkdirs(String path)
	{
		return (new File(path)).mkdirs();
	}

	//----- Section: XLSX related --------------------------------------------------------------------------------------

	/**
	 * Reads a worksheet from a .xlsx formatted file and returns it as a stream of String[].
	 *
	 * Code lifted from:
	 * http://java67.blogspot.com/2014/09/how-to-read-write-xlsx-file-in-java-apache-poi-example.html
	 * http://stackoverflow.com/questions/24511052/how-to-convert-an-iterator-to-a-stream
	 */
	public static Stream<String[]> getXLSXAsStream(String filename, String sheetName)
	{
		try
		{
			XSSFWorkbook myWorkBook = new XSSFWorkbook(new FileInputStream(new File(filename)));
			// Grab desired sheet from the XLSX workbook
			XSSFSheet mySheet = myWorkBook.getSheet(sheetName);
			// Get iterator to all the rows in the sheet
			Iterator<Row> rowIterator = mySheet.iterator();
			// Wrap that iterator with another that converts row to String[]
			XLSXRowIterator iter = new XLSXRowIterator(rowIterator);
			// Convert to stream
			Iterable<String[]> iterable = () -> iter;
			return StreamSupport.stream(iterable.spliterator(), false);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	static class XLSXRowIterator implements Iterator<String[]>
	{
		Iterator<Row> innerIter;

		public XLSXRowIterator(Iterator<Row> innerIter)
		{
			this.innerIter = innerIter;
		}

		@Override
		public boolean hasNext()
		{
			return innerIter.hasNext();
		}

		@Override
		public String[] next()
		{
			Row row = innerIter.next();
			List<String> list = new ArrayList<>();
			row.forEach(cell -> list.add(
				cell.getCellType() == Cell.CELL_TYPE_STRING ? cell.getStringCellValue() :
				cell.getCellType() == Cell.CELL_TYPE_NUMERIC ? Double.toString(cell.getNumericCellValue()) : ""));
			return list.toArray(new String[list.size()]);
		}
	}

	public static List<String> getXLSXSheetNames(String filename) { try
	{
		// Generates a workbook instance for XLSX file
		XSSFWorkbook myWorkBook = new XSSFWorkbook(new FileInputStream(new File(filename)));

		return IntStream.range(0, myWorkBook.getNumberOfSheets()).boxed().map(myWorkBook::getSheetName)
			.collect(Collectors.toList());
	}
	catch (IOException e){throw new RuntimeException(e);}}


	//--- Section: SIF files ------------------------------------------------------------------------------------------|

	public static void replaceNodeNamesInSIFFile(String filename, Map<String, String> substutitionMap) { try
	{
		String tmpFile = filename + ".tmp";
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(tmpFile));
		File file = new File(filename);
		Scanner sc = new Scanner(file);
		while (sc.hasNextLine())
		{
			String[] token = sc.nextLine().split("\t");
			if (token.length > 2)
			{
				if (substutitionMap.containsKey(token[0])) token[0] = substutitionMap.get(token[0]);
				if (substutitionMap.containsKey(token[2])) token[2] = substutitionMap.get(token[2]);
			}
			writer.write(token[0]);
			for (int i = 1; i < token.length; i++)
			{
				writer.write("\t" + token[i]);
			}
			writer.write("\n");
		}
		writer.close();

		file.delete();
		new File(tmpFile).renameTo(file);
	}
	catch (IOException e){throw new RuntimeException(e);}}

	public static Set<String> getNodeNamesInSIFFile(String filename) { try
	{
		return Files.lines(Paths.get(filename)).map(line -> line.split("\t")).filter(token -> token.length >= 3)
			.map(token -> new String[]{token[0], token[2]}).flatMap(Arrays::stream).collect(Collectors.toSet());
	}
	catch (IOException e){throw new RuntimeException(e);}}

	public static Set<String> getNodePairsInSIFFile(String filename) { try
	{
		return Files.lines(Paths.get(filename)).map(line -> line.split("\t")).filter(token -> token.length >= 3)
			.map(token -> token[0] + " " + token[2]).collect(Collectors.toSet());
	}
	catch (IOException e){throw new RuntimeException(e);}}

	public static boolean sameFiles(String file1, String file2) throws FileNotFoundException
	{
		Scanner sc1 = new Scanner(new File(file1));
		Scanner sc2 = new Scanner(new File(file2));

		while (sc1.hasNextLine())
		{
			if (!sc2.hasNextLine()) return false;
			if (!sc1.nextLine().equals(sc2.nextLine())) return false;
		}
		return true;
	}

	public static List<String> getSubdirectoriesContaining(String parent, String file)
	{
		File dir = new File(parent);
		if (!dir.isDirectory()) return Collections.emptyList();

		List<String> list = new ArrayList<>();

		File target = new File(parent + File.separator + file);
		if (target.exists() && !target.isDirectory()) list.add(parent);

		for (File child : dir.listFiles())
		{
			if (child.isDirectory()) list.addAll(getSubdirectoriesContaining(child.getPath(), file));
		}
		return list;
	}

	public static void main(String[] args) throws IOException
	{
//		printLines("/media/babur/6TB1/TCGA-pancan/whole/DataMatrix.txt", 1, 2);
//		printShortLines("/home/ozgun/Data/PC/v10/summary.txt", 200);

//		exciseFileToLines("/home/babur/Documents/TCGA/PanCan/mutation.maf", "/home/babur/Documents/Temp/temp.txt",
//			line -> line.startsWith("Hugo_Symbol\t") ||
//				(line.startsWith("MTOR\t") && !line.contains("\tIntron\t") && !line.contains("\tSilent\t")));
//					&& line.contains("\tp.R766")));


//		System.out.println(countLines("/home/babur/Projects/utility/PNNL-ovarian-correlations.txt"));
//		printLines("/home/babur/Documents/Analyses/TF-activity/MultipleMyeloma/Filtered_GSE47552_series_matrix.txt", 20000, 20008);
		printLines("/home/ozgun/Downloads/Homo_sapiens.GRCh37.87.gtf", "\"TP53\"", 100);
//		printLines("/home/ozgun/Downloads/Homo_sapiens.GRCh37.87.gtf", 0, 100);

//		exciseFileToLines("/home/babur/Documents/PC/SignedPC-p2-e2.sif", "/home/babur/Documents/Papers/Authoring/CausalPath/temp.sif", line -> line.contains("http://identifiers.org/reactome/R-HSA-1183067"));

//		System.out.println(countLines("/home/babur/Documents/Analyses/TF-activity/MultipleMyeloma/Filtered_GSE47552_series_matrix.txt"));

//		HashSet<String> query = new HashSet<>(Arrays.asList("Hugo_Symbol\t", "SP3\t"));
//		exciseFileToLines("/home/babur/Documents/TCGA/PanCan/mutation.maf", "/home/babur/Documents/Temp/SP3.maf",
//			line -> query.stream().anyMatch(line::startsWith));

//		countTermsInTabDelimitedColumn("/home/babur/Downloads/trrust_rawdata.txt", 2);

//		System.out.println(sameFiles("/home/babur/Documents/mutex/TCGA/PanCan/1/1/DataMatrix.txt", "/home/babur/Documents/mutex/TCGA/PanCan-shuffled/1/1/DataMatrix.txt"));
	}
}
