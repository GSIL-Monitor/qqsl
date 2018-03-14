package com.hysw.qqsl.cloud.core.service;

import org.osgeo.proj4j.*;
import org.springframework.stereotype.Service;

/**
 * 转换坐标service
 * @author 陈雷
 * @since 2015年3月16日
 */

@Service("transFromService")
public class TransFromService {
	private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
	CRSFactory crsFactory = new CRSFactory();
	static final String WGS84_PARAM = "+title=long/lat:WGS84 +proj=longlat +datum=WGS84 +units=degrees";
	CoordinateReferenceSystem WGS84 = crsFactory.createFromParameters("WGS84",
			WGS84_PARAM);

	/**
	 * 将大地坐标经纬度转换为平面坐标
	 * @return
	 */
	public ProjCoordinate BLHToXYZ(String code,double lon,double lat) {
		CoordinateTransform trans = ctFactory.createTransform(WGS84, createCRS(code));
		ProjCoordinate xyz = new ProjCoordinate();
		trans.transform(new ProjCoordinate(lon, lat), xyz);
		return xyz;
	}

	/**
	 * 将平面坐标转换为大地坐标经纬度
	 * @return
	 */
	public ProjCoordinate XYZToBLH(String code,double x,double y) {
		CoordinateTransform trans = ctFactory.createTransform(createCRS(code), createCRS(code).createGeographic());
		ProjCoordinate BLH = new ProjCoordinate();
		trans.transform(new ProjCoordinate(x, y), BLH);
		return BLH;
	}

	/**
	 * 查询ESRI参数名
	 * @param crsSpec
	 * @return
	 */
	private CoordinateReferenceSystem createCRS(String crsSpec) {
		CoordinateReferenceSystem crs = null;
		// test if name is a PROJ4 spec
		if (crsSpec.indexOf("+") >= 0 || crsSpec.indexOf("=") >= 0) {
			crs = crsFactory.createFromParameters("ESRI", crsSpec);
		} else {
			crs = crsFactory.createFromName(crsSpec);
		}
		return crs;
	}


	/**
	 * 根据获取的经度确认其参数值(80)
	 * @param lon
	 * @return
	 */
	public final String checkCode80(String lon){
		switch (Integer.valueOf(lon)) {
			case 75:
				return "ESRI:2370";
			case 78:
				return "ESRI:2371";
			case 81:
				return "ESRI:2372";
			case 84:
				return "ESRI:2373";
			case 87:
				return "ESRI:2374";
			case 90:
				return "ESRI:2375";
			case 93:
				return "ESRI:2376";
			case 96:
				return "ESRI:2377";
			case 99:
				return "ESRI:2378";
			case 102:
				return "ESRI:2379";
			case 105:
				return "ESRI:2380";
			case 108:
				return "ESRI:2381";
			case 111:
				return "ESRI:2382";
			case 114:
				return "ESRI:2383";
			case 117:
				return "ESRI:2384";
			case 120:
				return "ESRI:2385";
			case 123:
				return "ESRI:2386";
			case 126:
				return "ESRI:2387";
			case 129:
				return "ESRI:2388";
			case 132:
				return "ESRI:2389";
			case 135:
				return "ESRI:2390";
			default:
				return "ESRI:2379";
		}

	}

	/**
	 * 根据获取的经度确认其参数值(54)
	 * @param lon
	 * @return
	 */
	public final String checkCode54(String lon){
		switch (Integer.valueOf(lon)) {
			case 75:
				return "ESRI:2422";
			case 78:
				return "ESRI:2423";
			case 81:
				return "ESRI:2424";
			case 84:
				return "ESRI:2425";
			case 87:
				return "ESRI:2426";
			case 90:
				return "ESRI:2427";
			case 93:
				return "ESRI:2428";
			case 96:
				return "ESRI:2429";
			case 99:
				return "ESRI:2430";
			case 102:
				return "ESRI:2431";
			case 105:
				return "ESRI:2432";
			case 108:
				return "ESRI:2433";
			case 111:
				return "ESRI:2434";
			case 114:
				return "ESRI:2435";
			case 117:
				return "ESRI:2436";
			case 120:
				return "ESRI:2437";
			case 123:
				return "ESRI:2438";
			case 126:
				return "ESRI:2439";
			case 129:
				return "ESRI:2440";
			case 132:
				return "ESRI:2441";
			case 135:
				return "ESRI:2442";
			default:
				return "ESRI:2431";
		}

	}

	/**
	 * 根据获取的经度确认其参数值(84)
	 * @param lon
	 * @return
	 */
	public final String checkCode84(String lon){
		switch (Integer.valueOf(lon)) {
			case 75:
				return "ESRI:10000001";
			case 78:
				return "ESRI:10000002";
			case 81:
				return "ESRI:10000003";
			case 84:
				return "ESRI:10000004";
			case 87:
				return "ESRI:10000005";
			case 90:
				return "ESRI:10000006";
			case 93:
				return "ESRI:10000007";
			case 96:
				return "ESRI:10000008";
			case 99:
				return "ESRI:10000009";
			case 102:
				return "ESRI:10000010";
			case 105:
				return "ESRI:10000011";
			case 108:
				return "ESRI:10000012";
			case 111:
				return "ESRI:10000013";
			case 114:
				return "ESRI:10000014";
			case 117:
				return "ESRI:10000015";
			case 120:
				return "ESRI:10000016";
			case 123:
				return "ESRI:10000017";
			case 126:
				return "ESRI:10000018";
			case 129:
				return "ESRI:10000019";
			case 132:
				return "ESRI:10000020";
			case 135:
				return "ESRI:10000021";
			default:
				return "ESRI:10000010";
		}

	}


	/**
	 * 不同坐标系下的平面转大地 模糊
	 * @param csName
	 * @param lon
	 * @param lat
	 */
	public ProjCoordinate usedProj4jTransfrom(String csName, double lon, double lat,String sign){
		CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
		CRSFactory csFactory = new CRSFactory();

		CoordinateReferenceSystem crs = csFactory.createFromName(csName);

		final String WGS84_PARAM = "+title=long/lat:WGS84 +proj=longlat +ellps=WGS84 +datum=WGS84 +units=degrees";
		CoordinateReferenceSystem WGS84 = csFactory.createFromParameters("WGS84",WGS84_PARAM);
		CoordinateTransform trans = null;
		if(sign.toLowerCase().equals("plane".toLowerCase())){
			trans = ctFactory.createTransform(crs,WGS84);
		}else if(sign.toLowerCase().equals("ground".toLowerCase())){
			trans = ctFactory.createTransform(WGS84,crs);
		}

		ProjCoordinate p = new ProjCoordinate();
		ProjCoordinate p2 = new ProjCoordinate();
		p.x = lon;
		p.y = lat;

		trans.transform(p, p2);
		return p2;
	}

//	/**
//	 * 根据不同坐标系选择参数
//	 * @param baseLevelType
//	 *                    参数 beijing54 xian80 WGS84
//	 * @return
//	 */
//	public double[] selcetTransFromParam(Coordinate.BaseLevelType baseLevelType){
//		double[] d = new double[3];
//		if(baseLevelType== Coordinate.BaseLevelType.BEIJING54){
//			d[0] = 6378245d;
//			d[1] = 6356863d;
//			d[2] = (d[0] * d[0] - d[1] * d[1]) / (d[0] * d[0]);
//		}else if(baseLevelType== Coordinate.BaseLevelType.XIAN80){
//			d[0]=6378140d;//长轴
//			d[1]=6356755.2882d;//短轴
//			d[2] = (d[0] * d[0] - d[1] * d[1]) / (d[0] * d[0]);
//		}else if(baseLevelType== Coordinate.BaseLevelType.WGS84){
//			d[0]=6378137d;//长轴
//			d[1]=6356752.314d;//短轴
//			d[2] = (d[0] * d[0] - d[1] * d[1]) / (d[0] * d[0]);
//		} else if (baseLevelType == Coordinate.BaseLevelType.CGCS2000) {
//			d[0]=6378137d;//长轴
//			d[1]=6356752.31414d;//短轴
//			d[2] = (d[0] * d[0] - d[1] * d[1]) / (d[0] * d[0]);
//		}
//		return d;
//	}

//	/**
//	 * 将平面坐标转换为大地坐标
//	 * @param central
//	 * @param x
//	 * @param y
//	 * @param z
//	 * @return
//	 */
//	public ProjCoordinate transFromPlaneToGround(Coordinate.BaseLevelType baseLevelType, String central, double x, double y, double z) {
//		if(Double.valueOf(central)%3!=0){
//			return null;
//		}
//		String code = null;
//		if (baseLevelType == Coordinate.BaseLevelType.BEIJING54) {
//			code = checkCode54(central);
//		} else if (baseLevelType == Coordinate.BaseLevelType.XIAN80) {
//			code = checkCode80(central);
//		} else if (baseLevelType == Coordinate.BaseLevelType.WGS84) {
//			code = checkCode84(central);
//		} else if (baseLevelType == Coordinate.BaseLevelType.CGCS2000) {
////			code=checkCode2000(central)
//		}
//		ProjCoordinate projCoordinate =XYZToBLH(code, x, y);
//		ProjCoordinate projCoordinate1 = new ProjCoordinate();
//		projCoordinate1.setValue(projCoordinate.y, projCoordinate.x, z);
//		return projCoordinate1;
//	}

//	/**
//	 * 将大地坐标转换为平面坐标
//	 *
//	 * @param central
//	 * @param lat
//	 * @param lon
//	 * @param ele
//	 * @return
//	 */
//	public ProjCoordinate transFromGroundToPlane(Coordinate.BaseLevelType baseLevelType, String central, double lat, double lon, double ele) {
//		if (Double.valueOf(central) % 3 != 0) {
//			return null;
//		}
//		String code = null;
//		if (baseLevelType == Coordinate.BaseLevelType.BEIJING54) {
//			code = checkCode54(central);
//		} else if (baseLevelType == Coordinate.BaseLevelType.XIAN80) {
//			code = checkCode80(central);
//		} else if (baseLevelType == Coordinate.BaseLevelType.WGS84) {
//			code = checkCode84(central);
//		} else if (baseLevelType == Coordinate.BaseLevelType.CGCS2000) {
////			code=checkCode2000(central)
//		}
//		ProjCoordinate projCoordinate = BLHToXYZ(code, lon, lat);
//		ProjCoordinate projCoordinate1 = new ProjCoordinate();
//		projCoordinate1.setValue(projCoordinate.y, projCoordinate.x, ele);
//		return projCoordinate1;
//	}

	/**
	 * 将大地坐标转换为平面坐标
	 * @param central
	 * @param lat
	 * @param lon
	 * @return
	 */
//	public double[] transFromGroundToPlane(Coordinate.BaseLevelType baseLevelType, String central, double lat, double lon) {
//		if(Double.valueOf(central)%3!=0){
//			return null;
//		}
//		String code = null;
//		if (baseLevelType == Coordinate.BaseLevelType.BEIJING54) {
//			code = checkCode54(central);
//		} else if (baseLevelType == Coordinate.BaseLevelType.XIAN80) {
//			code = checkCode80(central);
//		} else if (baseLevelType == Coordinate.BaseLevelType.WGS84) {
//			code = checkCode84(central);
//		} else if (baseLevelType == Coordinate.BaseLevelType.CGCS2000) {
////			code=checkCode2000(central)
//		}
//		ProjCoordinate projCoordinate = BLHToXYZ(code, lon, lat);
//		double[] d = {projCoordinate.y,projCoordinate.x};
//		return d;
//	}

	/**
	 * 转换空间直接坐标
	 * @param d
	 * @param projCoordinate
	 * @return
	 */
	public double[] transFromRectangularSpaceCoordinate(double[] d,ProjCoordinate projCoordinate) {
		double N = d[0] / (Math.sqrt(1 - d[2] * Math.sin(projCoordinate.x*Math.PI/180) * Math.sin(projCoordinate.x*Math.PI/180)));
		double x=(N+projCoordinate.z)* Math.cos(projCoordinate.x * Math.PI / 180) * Math.cos(projCoordinate.y * Math.PI / 180);
		double y=(N+projCoordinate.z)* Math.cos(projCoordinate.x * Math.PI / 180) * Math.sin(projCoordinate.y * Math.PI / 180);
		double z=((1-d[2])*N+projCoordinate.z)*Math.sin(projCoordinate.x * Math.PI / 180);
		double[] coordinate = new double[3];
		coordinate[0] = x;
		coordinate[1] = y;
		coordinate[2] = z;
		return coordinate;
	}

	//转换大地坐标
	public double[] transFromgeodeticCoordinate(double[] d,double X,double Y,double Z) {
		double L=Math.toDegrees(Math.atan(Y/X)+Math.PI);
		double B2=Math.atan(Z/Math.sqrt(X*X+Y*Y));
		double B1;
		double N;
		while (true){
			N=d[0]/Math.sqrt(1-d[2]*Math.sin(B2)*Math.sin(B2));
			B1=Math.atan((Z+N*d[2]*Math.sin(B2))/Math.sqrt(X*X+Y*Y));
			if(Math.abs(B1-B2)<0.0000000001)
				break;
			B2=B1;
		}
		double H=Z/Math.sin(B2)-N*(1-d[2]);
		double B=Math.toDegrees(B2);
		return new double[]{B, L, H};
	}

	/**
	 * 计算7参数
	 * @param central
	 * @param baseLevelType
	 * @param param
	 * @param param84
	 */
//	public Matrix calculate7Param(String central, Coordinate.BaseLevelType baseLevelType, double[][] param, double[][] param84) {
//		//将平面坐标转换为大地坐标
//		ProjCoordinate projCoordinate1 = transFromPlaneToGround(baseLevelType,central, param[0][1], param[0][0], param[0][2]);
//		ProjCoordinate projCoordinate2 = transFromPlaneToGround(baseLevelType,central, param[1][1], param[1][0], param[1][2]);
//		ProjCoordinate projCoordinate3 = transFromPlaneToGround(baseLevelType,central, param[2][1], param[2][0], param[2][2]);
//		//转换空间直接坐标
//		double[] fg = transFromRectangularSpaceCoordinate(selcetTransFromParam(baseLevelType), projCoordinate1);
//		double[] sg = transFromRectangularSpaceCoordinate(selcetTransFromParam(baseLevelType), projCoordinate2);
//		double[] tg = transFromRectangularSpaceCoordinate(selcetTransFromParam(baseLevelType), projCoordinate3);
//		//将坐标放入projcoordinate
//		ProjCoordinate projCoordinate4 = setParamToProjcoordinate(param84[0][0], param84[0][1], param84[0][2]);
//		ProjCoordinate projCoordinate5 = setParamToProjcoordinate(param84[1][0], param84[1][1], param84[1][2]);
//		ProjCoordinate projCoordinate6 = setParamToProjcoordinate(param84[2][0], param84[2][1], param84[2][2]);
//		//转换空间直接坐标
//		double[] fg84 = transFromRectangularSpaceCoordinate(selcetTransFromParam(Coordinate.BaseLevelType.WGS84), projCoordinate4);
//		double[] sg84 = transFromRectangularSpaceCoordinate(selcetTransFromParam(Coordinate.BaseLevelType.WGS84), projCoordinate5);
//		double[] tg84 = transFromRectangularSpaceCoordinate(selcetTransFromParam(Coordinate.BaseLevelType.WGS84), projCoordinate6);
//
//		double[][] C = {
//				{1, 0, 0, fg[0], 0, -fg[2], fg[1]},
//				{0, 1, 0, fg[1], fg[2], 0, -fg[0]},
//				{0, 0, 1, fg[2], -fg[1], fg[0], 0},
//				{1, 0, 0, sg[0], 0, -sg[2], sg[1]},
//				{0, 1, 0, sg[1], sg[2], 0, -sg[0]},
//				{0, 0, 1, sg[2], -sg[1], sg[0], 0},
//				{1, 0, 0, tg[0], 0, -tg[2], tg[1]},
//				{0, 1, 0, tg[1], tg[2], 0, -tg[0]},
//				{0, 0, 1, tg[2], -tg[1], tg[0], 0},
//		};
//		double[][] b = {
//				{fg84[0] - fg[0]},
//				{fg84[1] - fg[1]},
//				{fg84[2] - fg[2]},
//				{sg84[0] - sg[0]},
//				{sg84[1] - sg[1]},
//				{sg84[2] - sg[2]},
//				{tg84[0] - tg[0]},
//				{tg84[1] - tg[1]},
//				{tg84[2] - tg[2]},
//		};
//		Matrix A = new Matrix(C);
//		Matrix B = new Matrix(b);
//		Matrix AT = A.transpose();
//		Matrix R = (AT.times(A)).inverse().times(AT).times(B);
//		return R;
//	}

	/**
	 * 计算4参数
	 *
	 * @param param54
	 */
//	public Matrix calculate4Param(String central, double[][] param54, double[][] param84) {
//		//将大地坐标转换为平面坐标
//		double[] fg84 = transFromGroundToPlane(Coordinate.BaseLevelType.WGS84,central, param84[0][0], param84[0][1]);
//		double[] sg84 = transFromGroundToPlane(Coordinate.BaseLevelType.WGS84,central, param84[1][0], param84[1][1]);
//
//		double[][] C = {
//				{1, 0, -fg84[1], fg84[0]},
//				{0, 1, fg84[0], fg84[1]},
//				{1, 0, -sg84[1], sg84[0]},
//				{0, 1, sg84[0], sg84[1]},
//		};
//		double[][] b = {
//				{param54[0][0] - fg84[0]},
//				{param54[0][1] - fg84[1]},
//				{param54[1][0] - sg84[0]},
//				{param54[1][1] - sg84[1]},
//		};
//		Matrix A = new Matrix(C);
//		Matrix B = new Matrix(b);
//		Matrix AT = A.transpose();
//		Matrix R = (AT.times(A)).inverse().times(AT).times(B);
//		return R;
//	}

	/**
	 * 将坐标放入projcoordinate
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private ProjCoordinate setParamToProjcoordinate(double x, double y, double z) {
		ProjCoordinate projCoordinate = new ProjCoordinate();
		projCoordinate.x = x;
		projCoordinate.y = y;
		projCoordinate.z = z;
		return projCoordinate;
	}

}
